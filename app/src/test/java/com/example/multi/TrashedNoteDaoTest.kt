package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.multi.data.EventDatabase
import com.example.multi.data.TrashedNoteEntity
import com.example.multi.data.NoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrashedNoteDaoTest {
    private lateinit var db: EventDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deleteAll_removesOnlyTrashedNotes() = runBlocking {
        val trashedDao = db.trashedNoteDao()
        val noteDao = db.noteDao()
        trashedDao.insert(TrashedNoteEntity(header = "h1", content = "c1", created = 0L, deleted = 0L))
        trashedDao.insert(TrashedNoteEntity(header = "h2", content = "c2", created = 0L, deleted = 0L))
        noteDao.insert(NoteEntity(header = "n1", content = "c3", created = 0L, lastOpened = 0L))
        assertEquals(2, trashedDao.getNotes().size)
        assertEquals(1, noteDao.getNotes().size)
        trashedDao.deleteAll()
        assertEquals(0, trashedDao.getNotes().size)
        assertEquals(1, noteDao.getNotes().size)
    }
}
