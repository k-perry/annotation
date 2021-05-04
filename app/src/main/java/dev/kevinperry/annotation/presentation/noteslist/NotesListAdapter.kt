package dev.kevinperry.annotation.presentation.noteslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.kevinperry.annotation.data.model.Note
import dev.kevinperry.annotation.databinding.ItemNoteBinding

class NotesListAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<Note, NotesListAdapter.NotesViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class NotesViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val note = getItem(position)
                        listener.onItemClicked(note)
                    }
                }
            }
        }

        fun bind(note: Note) {
            binding.apply {
                textViewTitle.text = note.title
                textViewPreview.text = note.contentPreview
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
            (oldItem.id == newItem.id)

        // Equals automatically implemented since Note is data class
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
            (oldItem == newItem)
    }

    interface OnItemClickListener {
        fun onItemClicked(note: Note)
    }
}