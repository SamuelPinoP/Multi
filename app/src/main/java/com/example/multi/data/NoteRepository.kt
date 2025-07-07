package com.example.multi.data

import android.content.Context
import com.example.multi.Note
import com.example.multi.TrashedNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository private constructor(context: Context) {
    private val db = EventDatabase.getInstance(context)
    private val noteDao = db.noteDao()
    private val trashDao = db.trashedNoteDao()

    suspend fun getNotes(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getNotes().map { it.toModel() }
    }

    suspend fun insert(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insert(note.toEntity())
    }

    suspend fun update(note: Note) = withContext(Dispatchers.IO) {
        noteDao.update(note.toEntity())
    }

    suspend fun delete(note: Note) = withContext(Dispatchers.IO) {
        noteDao.delete(note.toEntity())
    }

    suspend fun moveToTrash(note: Note) = withContext(Dispatchers.IO) {
        trashDao.insert(
            TrashedNote(
                header = note.header,
                content = note.content,
                created = note.created
            ).toEntity()
        )
        noteDao.delete(note.toEntity())
    }

    suspend fun clearExpiredTrash(threshold: Long) = withContext(Dispatchers.IO) {
        trashDao.deleteExpired(threshold)
    }

    companion object {
        @Volatile private var INSTANCE: NoteRepository? = null

        fun getInstance(context: Context): NoteRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
