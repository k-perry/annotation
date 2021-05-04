package dev.kevinperry.annotation.presentation.noteslist

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kevinperry.annotation.data.model.Note
import dev.kevinperry.annotation.data.db.NoteDao
import dev.kevinperry.annotation.data.PreferencesManager
import dev.kevinperry.annotation.data.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")
    private val preferencesFlow = preferencesManager.preferencesFlow

    // Use channel to for ViewModel to send messages to fragment, but not other way around.
    // Only expose a flow instead of the channel itself to prevent fragment from sending something
    // in channel.
    private val notesListEventChannel = Channel<NotesListEvent>()
    val notesListEventFlow = notesListEventChannel.receiveAsFlow()

    @ExperimentalCoroutinesApi
    private val notesFlow =
        combine(searchQuery.asFlow(), preferencesFlow) { searchQuery, preferences ->
            Pair(searchQuery, preferences)
        }.flatMapLatest { (searchQuery, preferences) ->
            noteDao.getNotes(searchQuery, preferences.sortOrder)
        }

    @ExperimentalCoroutinesApi
    val notes = notesFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onNoteClicked(note: Note) = viewModelScope.launch {
        notesListEventChannel.send(NotesListEvent.NavigateToEditNoteScreen(note))
    }

    fun onNoteSwiped(note: Note) = viewModelScope.launch {
        noteDao.delete(note)
        notesListEventChannel.send(NotesListEvent.ShowUndoDeleteNoteMessage(note))
    }

    fun onUndoDeleteClicked(note: Note) = viewModelScope.launch {
        // Add same item back to database
        noteDao.insert(note)
    }

    fun onAddNewNoteClicked() = viewModelScope.launch {
        notesListEventChannel.send(NotesListEvent.NavigateToAddNoteScreen)
    }

    // Events to send to Fragment
    sealed class NotesListEvent {
        object NavigateToAddNoteScreen: NotesListEvent()
        data class NavigateToEditNoteScreen(val note: Note): NotesListEvent()
        data class ShowUndoDeleteNoteMessage(val note: Note): NotesListEvent()
    }

}