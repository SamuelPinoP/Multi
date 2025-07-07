package com.example.multi.repository

import com.example.multi.Note
import com.example.multi.TrashedNote
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel

/**
 * Repository providing note related data operations.
 */
class NoteRepository(private val db: EventDatabase) {
    private val noteDao = db.noteDao()
    private val trashDao = db.trashedNoteDao()

    suspend fun getNotes(): List<Note> =
        noteDao.getNotes().map { it.toModel() }

    suspend fun insert(note: Note): Long =
        noteDao.insert(note.toEntity())

    suspend fun update(note: Note) =
        noteDao.update(note.toEntity())

    suspend fun delete(note: Note) =
        noteDao.delete(note.toEntity())

    suspend fun moveToTrash(note: Note) {
        trashDao.insert(
            TrashedNote(
                header = note.header,
                content = note.content,
                created = note.created
            ).toEntity()
        )
        noteDao.delete(note.toEntity())
    }

    suspend fun getTrashedNotes(): List<TrashedNote> =
        trashDao.getNotes().map { it.toModel() }

    suspend fun deleteExpiredTrash(threshold: Long) =
        trashDao.deleteExpired(threshold)

    suspend fun restore(note: TrashedNote) {
        noteDao.insert(
            Note(
                header = note.header,
                content = note.content,
                created = note.created,
                lastOpened = System.currentTimeMillis()
            ).toEntity()
        )
        trashDao.delete(note.toEntity())
    }

    suspend fun deleteForever(note: TrashedNote) =
        trashDao.delete(note.toEntity())
}
