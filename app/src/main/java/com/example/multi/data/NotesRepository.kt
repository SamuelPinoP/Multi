package com.example.multi.data

import com.example.multi.Note
import com.example.multi.TrashedNote
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Single source of truth for the Notes feature.
 *
 * The repository hides Room from the rest of the app: callers work with the
 * plain [Note] domain model and never see [NoteEntity] or a [NoteDao]. This is
 * the seam that lets the UI layer be unit-tested against a fake implementation
 * ([FakeNotesRepository] in the test source set) without an Android device.
 */
interface NotesRepository {

    /** Emits the full note list (newest first) and re-emits on every change. */
    fun observeNotes(): Flow<List<Note>>

    /** One-shot read, primarily for widgets and background work. */
    suspend fun getNotes(): List<Note>

    /** Inserts [note] and returns the generated row id. */
    suspend fun insert(note: Note): Long

    /** Persists edits to an existing [note]. */
    suspend fun update(note: Note)

    /**
     * Moves [notes] to the trash bin: each note is copied into `trashed_notes`
     * with a deletion timestamp and then removed from the active table.
     */
    suspend fun moveToTrash(notes: List<Note>)

    /** Permanently removes trashed notes older than [retentionMillis]. */
    suspend fun purgeExpiredTrash(retentionMillis: Long = DEFAULT_TRASH_RETENTION_MILLIS)

    companion object {
        /** Trashed notes are kept for 30 days before being purged. */
        const val DEFAULT_TRASH_RETENTION_MILLIS: Long = 30L * 24 * 60 * 60 * 1000
    }
}

/**
 * Room-backed [NotesRepository].
 *
 * @param ioDispatcher injected so tests can pump work on a deterministic
 *   scheduler; defaults to [Dispatchers.IO] in production.
 * @param now injected clock, keeps trash timestamps testable.
 */
class RoomNotesRepository(
    private val noteDao: NoteDao,
    private val trashedNoteDao: TrashedNoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val now: () -> Long = System::currentTimeMillis,
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeNotes().map { entities -> entities.map(NoteEntity::toModel) }

    override suspend fun getNotes(): List<Note> = withContext(ioDispatcher) {
        noteDao.getNotes().map(NoteEntity::toModel)
    }

    override suspend fun insert(note: Note): Long = withContext(ioDispatcher) {
        noteDao.insert(note.toEntity())
    }

    override suspend fun update(note: Note): Unit = withContext(ioDispatcher) {
        noteDao.update(note.toEntity())
    }

    override suspend fun moveToTrash(notes: List<Note>): Unit = withContext(ioDispatcher) {
        val deletedAt = now()
        notes.forEach { note ->
            trashedNoteDao.insert(
                TrashedNote(
                    header = note.header,
                    content = note.content,
                    created = note.created,
                    deleted = deletedAt,
                    attachmentUri = note.attachmentUri,
                ).toEntity()
            )
            noteDao.delete(note.toEntity())
        }
    }

    override suspend fun purgeExpiredTrash(retentionMillis: Long): Unit = withContext(ioDispatcher) {
        trashedNoteDao.deleteExpired(now() - retentionMillis)
    }
}
