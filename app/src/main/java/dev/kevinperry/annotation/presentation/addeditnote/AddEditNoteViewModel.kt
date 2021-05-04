package dev.kevinperry.annotation.presentation.addeditnote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kevinperry.annotation.data.model.Note
import dev.kevinperry.annotation.data.db.NoteDao
import dev.kevinperry.annotation.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteDao: NoteDao,
    state: SavedStateHandle,
    // Use ApplicationScope for saving to DB so scope can't be canceled if user leaves
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    lateinit var note: Note
    private var noteId = 0
    private var saveTimer = Timer()

    // Use channel to for ViewModel to send messages to fragment, but not other way around.
    // Only expose a flow instead of the channel itself to prevent fragment from sending something
    // in channel.
    private val addEditNoteEventChannel = Channel<AddEditNoteEvent>()
    val addEditNoteEventFlow = addEditNoteEventChannel.receiveAsFlow()

    companion object {
        private const val SAVE_DELAY = 500L // milliseconds
    }

    init {
        val notePassedIn = state.get<Note>("note")
        if (notePassedIn != null) {
            // Edit existing note
            note = notePassedIn
            noteId = note.id
        } else {
            // Create new note
            createNewNote()
        }
    }

    fun onTitleChanged(title: String) = saveNoteAfterDelay(title = title)

    fun onNoteChanged(content: String) = saveNoteAfterDelay(content = content)

    private fun saveNoteAfterDelay(title: String? = null, content: String? = null) {
        applicationScope.launch {
            addEditNoteEventChannel.send(AddEditNoteEvent.ShowSavingStatus)
            saveTimer.cancel()
            saveTimer = Timer()
            saveTimer.schedule(SAVE_DELAY) {
                applicationScope.launch {
                    if (title != null) {
                        note = note.copy(id = noteId, title = title)
                    } else if (content != null) {
                        note = note.copy(id = noteId, content = content)
                    }
                    noteDao.update(note)
                    addEditNoteEventChannel.send(AddEditNoteEvent.ShowSavedStatus)
                }
            }
        }
    }

    private fun createNewNote() = viewModelScope.launch {
        note = Note()
        noteId = noteDao.insert(note).toInt()
    }

    // Events to send to Fragment
    sealed class AddEditNoteEvent {
        object ShowSavingStatus : AddEditNoteEvent()
        object ShowSavedStatus : AddEditNoteEvent()
    }

}