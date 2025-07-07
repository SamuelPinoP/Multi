package com.example.multi.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesRepository(private val db: EventDatabase) {
    private val noteDao get() = db.noteDao()
    private val trashDao get() = db.trashedNoteDao()

    suspend fun getNotes() = withContext(Dispatchers.IO) { noteDao.getNotes() }

    suspend fun insert(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        noteDao.insert(note)
    }

    suspend fun update(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.update(note)
    }

    suspend fun delete(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.delete(note)
    }

    suspend fun deleteExpiredTrash(threshold: Long) = withContext(Dispatchers.IO) {
        trashDao.deleteExpired(threshold)
    }

    suspend fun addToTrash(note: TrashedNoteEntity) = withContext(Dispatchers.IO) {
        trashDao.insert(note)
    }
}
