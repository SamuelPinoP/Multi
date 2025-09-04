package com.example.multi

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.multi.data.EventDatabase
import com.example.multi.data.TrashedNoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrashedNoteDaoTest {
    private lateinit var db: EventDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EventDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deleteAllClearsNotes() = runBlocking {
        val dao = db.trashedNoteDao()
        dao.insert(TrashedNoteEntity(header = "h1", content = "c1", created = 0L, deleted = 0L))
        dao.insert(TrashedNoteEntity(header = "h2", content = "c2", created = 0L, deleted = 0L))
        assertEquals(2, dao.getNotes().size)
        dao.deleteAll()
        assertEquals(0, dao.getNotes().size)
    }
}
