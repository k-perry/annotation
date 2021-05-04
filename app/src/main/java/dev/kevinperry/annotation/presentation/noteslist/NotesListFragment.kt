package dev.kevinperry.annotation.presentation.noteslist

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.kevinperry.annotation.R
import dev.kevinperry.annotation.data.model.Note
import dev.kevinperry.annotation.data.SortOrder
import dev.kevinperry.annotation.databinding.FragmentNotesListBinding
import dev.kevinperry.annotation.util.onQueryTextChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class NotesListFragment : Fragment(R.layout.fragment_notes_list),
    NotesListAdapter.OnItemClickListener {

    private val viewModel: NotesListViewModel by viewModels()
    private lateinit var searchView: SearchView

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNotesListBinding.bind(view)
        val notesAdapter = NotesListAdapter(this)

        binding.apply {
            recyclerViewNotes.apply {
                adapter = notesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                addItemDecoration(
                    NotesListItemDecoration(
                        resources.getDimension(R.dimen.list_margin).toInt()
                    )
                )
            }

            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // Not needed, since we only care about swipe
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val note = notesAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onNoteSwiped(note)
                }
            }).attachToRecyclerView(recyclerViewNotes)

            fabAddNote.setOnClickListener {
                viewModel.onAddNewNoteClicked()
            }
        }

        viewModel.notes.observe(viewLifecycleOwner) {
            // Update adapter with new data set after changes occur
            notesAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.notesListEventFlow.collect { event ->
                handleEvent(event)
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_notes_list, menu)

        val searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (!pendingQuery.isNullOrEmpty()) {
            // Restore previous search query after fragment is destroyed
            searchMenuItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_title -> {
                viewModel.onSortOrderSelected(SortOrder.BY_TITLE)
                true
            }
            R.id.action_sort_by_newest -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NEWEST)
                true
            }
            R.id.action_sort_by_oldest -> {
                viewModel.onSortOrderSelected(SortOrder.BY_OLDEST)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(note: Note) {
        viewModel.onNoteClicked(note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener so we don't get an empty string as query when destroyed (Android bug)
        searchView.setOnQueryTextListener(null)
    }

    private fun handleEvent(event: NotesListViewModel.NotesListEvent) {
        when (event) {
            is NotesListViewModel.NotesListEvent.ShowUndoDeleteNoteMessage -> showUndoDeleteMessage(event)
            is NotesListViewModel.NotesListEvent.NavigateToAddNoteScreen -> navigateToAddNoteScreen()
            is NotesListViewModel.NotesListEvent.NavigateToEditNoteScreen -> navigateToEditNoteScreen(event)
        }
    }

    private fun navigateToEditNoteScreen(event: NotesListViewModel.NotesListEvent.NavigateToEditNoteScreen) {
        val action =
            NotesListFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                event.note
            )
        findNavController().navigate(action)
    }

    private fun navigateToAddNoteScreen() {
        val action =
            NotesListFragmentDirections.actionNotesFragmentToAddEditNoteFragment(
                null
            )
        findNavController().navigate(action)
    }

    private fun showUndoDeleteMessage(event: NotesListViewModel.NotesListEvent.ShowUndoDeleteNoteMessage) {
        Snackbar.make(requireView(), "Note deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.onUndoDeleteClicked(event.note)
            }
            .show()
    }

}