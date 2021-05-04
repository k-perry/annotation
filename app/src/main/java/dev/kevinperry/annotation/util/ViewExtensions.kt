package dev.kevinperry.annotation.util

import androidx.appcompat.widget.SearchView

// Extension function for simplified SearchView text change listener
inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            // Do nothing, since we search as text is changed
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}