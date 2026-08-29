package com.example.multi.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.multi.Note
import com.example.multi.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Immutable snapshot the Notes screen renders from. */
data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && notes.isEmpty()
}

/**
 * Owns all Notes-list state and business logic. The Activity became a thin
 * renderer: it collects [uiState] and forwards user intents to the functions
 * below. Because the list is driven by a Room [kotlinx.coroutines.flow.Flow],
 * edits made anywhere in the app (the editor, the widget, the trash bin) show
 * up here automatically — the old `onResume` reload hack is gone.
 */
class NotesViewModel(
    private val repository: NotesRepository,
) : ViewModel() {

    private val transientError = MutableStateFlow<String?>(null)

    private val notesStream = repository.observeNotes()
        .onEach { transientError.value = null }
        .catch { throwable ->
            transientError.value = throwable.message ?: "Could not load notes"
            emit(emptyList())
        }

    val uiState: StateFlow<NotesUiState> =
        combine(notesStream, transientError) { notes, error ->
            NotesUiState(notes = notes, isLoading = false, errorMessage = error)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = NotesUiState(),
        )

    init {
        // Housekeeping: permanently drop notes that outlived the 30-day trash window.
        viewModelScope.launch {
            runCatching { repository.purgeExpiredTrash() }
        }
    }

    /** User picked one or more notes and tapped "Delete". */
    fun moveToTrash(noteIds: Set<Long>) {
        if (noteIds.isEmpty()) return
        val targets = uiState.value.notes.filter { it.id in noteIds }
        viewModelScope.launch {
            runCatching { repository.moveToTrash(targets) }
                .onFailure { transientError.value = it.message ?: "Delete failed" }
        }
    }

    /** A file was imported from the system picker and should become a note. */
    fun importNote(header: String, attachmentUri: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            runCatching {
                repository.insert(
                    Note(
                        header = header,
                        content = "",
                        created = now,
                        lastOpened = now,
                        attachmentUri = attachmentUri,
                    )
                )
            }.onFailure { transientError.value = it.message ?: "Import failed" }
        }
    }

    fun consumeError() {
        transientError.value = null
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5_000L

        /** Factory so the Activity can hand in the repository from the [ServiceLocator]. */
        fun factory(repository: NotesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                        "Unknown ViewModel class: ${modelClass.name}"
                    }
                    return NotesViewModel(repository) as T
                }
            }
    }
}
