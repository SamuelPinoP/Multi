package com.example.multi.repository

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.multi.Note
import com.example.multi.TrashedNote
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel exposing notes and trash operations.
 */
class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository =
        NoteRepository(EventDatabase.getInstance(application.applicationContext))

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _trashed = MutableStateFlow<List<TrashedNote>>(emptyList())
    val trashed: StateFlow<List<TrashedNote>> = _trashed.asStateFlow()

    fun refreshNotes() {
        viewModelScope.launch {
            _notes.value = withContext(Dispatchers.IO) { repository.getNotes() }
        }
    }

    fun refreshTrash() {
        viewModelScope.launch {
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { repository.deleteExpiredTrash(threshold) }
            _trashed.value = withContext(Dispatchers.IO) { repository.getTrashedNotes() }
        }
    }

    fun moveToTrash(targets: List<Note>) {
        viewModelScope.launch(Dispatchers.IO) {
            targets.forEach { repository.moveToTrash(it) }
            refreshNotes()
        }
    }

    fun restore(note: TrashedNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restore(note)
            refreshTrash()
            refreshNotes()
        }
    }

    fun deleteForever(note: TrashedNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteForever(note)
            refreshTrash()
        }
    }
}
