package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.multi.data.EventDatabase
import com.example.multi.data.TrashedNoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrashedNoteDaoTest {
    private lateinit var db: EventDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun deleteAllClearsNotes() = runBlocking {
        val dao = db.trashedNoteDao()
        dao.insert(TrashedNoteEntity(header = "h1", content = "c1", created = 0L, deleted = 0L))
        dao.insert(TrashedNoteEntity(header = "h2", content = "c2", created = 0L, deleted = 0L))
        dao.deleteAll()
        assertTrue(dao.getNotes().isEmpty())
    }
}
