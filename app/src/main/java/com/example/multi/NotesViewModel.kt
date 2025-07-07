package com.example.multi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.multi.data.EventDatabase
import com.example.multi.data.NotesRepository
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotesRepository(EventDatabase.getInstance(application))

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    fun loadNotes() {
        viewModelScope.launch {
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            repository.deleteExpiredTrash(threshold)
            _notes.value = repository.getNotes().map { it.toModel() }
        }
    }

    fun deleteNotes(targets: List<Note>) {
        viewModelScope.launch {
            targets.forEach { note ->
                repository.addToTrash(
                    TrashedNote(
                        header = note.header,
                        content = note.content,
                        created = note.created
                    ).toEntity()
                )
                repository.delete(note.toEntity())
            }
            _notes.value = _notes.value.filterNot { note -> note.id in targets.map { it.id } }
        }
    }
}
