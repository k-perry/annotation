package dev.kevinperry.annotation.presentation.addeditnote

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.kevinperry.annotation.R
import dev.kevinperry.annotation.databinding.FragmentAddEditNoteBinding
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditNoteFragment : Fragment(R.layout.fragment_add_edit_note) {

    private val viewModel: AddEditNoteViewModel by viewModels()
    private lateinit var progressBarSaving: ProgressBar
    private lateinit var imageViewSaved: ImageView
    private lateinit var textViewSaveStatus: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditNoteBinding.bind(view)

        binding.apply {
            viewModel.note.apply {
                editTextTitle.setText(title)
                editTextNote.setText(content)
                textViewDateCreated.text = "Created: $formattedDateCreated"
            }
            editTextTitle.addTextChangedListener {
                viewModel.onTitleChanged(title = it.toString())
            }
            editTextNote.addTextChangedListener {
                viewModel.onNoteChanged(content = it.toString())
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditNoteEventFlow.collect { event ->
                handleEvent(event)
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_add_edit_note, menu)
        val itemSaveStatus: MenuItem = menu.findItem(R.id.item_save_status)
        progressBarSaving = itemSaveStatus.actionView.findViewById(R.id.progress_bar_saving)
        imageViewSaved = itemSaveStatus.actionView.findViewById(R.id.image_view_saved)
        textViewSaveStatus = itemSaveStatus.actionView.findViewById(R.id.text_view_save_status)
        showSavedStatus()
    }

    private fun handleEvent(event: AddEditNoteViewModel.AddEditNoteEvent) {
        when (event) {
            is AddEditNoteViewModel.AddEditNoteEvent.ShowSavingStatus -> showSavingStatus()
            is AddEditNoteViewModel.AddEditNoteEvent.ShowSavedStatus -> showSavedStatus()
        }
    }

    private fun showSavingStatus() {
        imageViewSaved.visibility = View.GONE
        progressBarSaving.visibility = View.VISIBLE
        textViewSaveStatus.setText(R.string.saving)
    }

    private fun showSavedStatus() {
        imageViewSaved.visibility = View.VISIBLE
        progressBarSaving.visibility = View.GONE
        textViewSaveStatus.setText(R.string.saved)
    }

}
