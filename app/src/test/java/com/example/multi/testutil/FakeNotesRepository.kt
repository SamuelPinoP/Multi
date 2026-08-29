package com.example.multi.testutil

import com.example.multi.Note
import com.example.multi.data.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [NotesRepository] for fast, deterministic unit tests. No Room, no
 * Android, no Robolectric — just a [MutableStateFlow] that behaves like Room's
 * change notifications.
 */
class FakeNotesRepository(initial: List<Note> = emptyList()) : NotesRepository {

    private val notes = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    /** Notes that were moved to trash, most recent last. */
    val trashed = mutableListOf<Note>()

    var purgeCallCount = 0
        private set

    /** When true, [observeNotes] terminates with an error to exercise error UI. */
    var failObserve = false

    override fun observeNotes(): Flow<List<Note>> {
        if (failObserve) {
            return kotlinx.coroutines.flow.flow { throw IllegalStateException("boom") }
        }
        return notes.asStateFlow()
    }

    override suspend fun getNotes(): List<Note> = notes.value

    override suspend fun insert(note: Note): Long {
        val id = nextId++
        notes.value = notes.value + note.copy(id = id)
        return id
    }

    override suspend fun update(note: Note) {
        notes.value = notes.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun moveToTrash(notes: List<Note>) {
        trashed.addAll(notes)
        val ids = notes.map { it.id }.toSet()
        this.notes.value = this.notes.value.filterNot { it.id in ids }
    }

    override suspend fun purgeExpiredTrash(retentionMillis: Long) {
        purgeCallCount++
    }
}
