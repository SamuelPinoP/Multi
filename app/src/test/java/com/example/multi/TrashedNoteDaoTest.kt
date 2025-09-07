package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.multi.data.EventDatabase
import com.example.multi.data.TrashedNoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun deleteAll_clearsNotes() = runBlocking {
        val dao = db.trashedNoteDao()
        dao.insert(TrashedNoteEntity(header = "a", content = "b", created = 0L, deleted = 0L))
        dao.insert(TrashedNoteEntity(header = "c", content = "d", created = 0L, deleted = 0L))
        assertEquals(2, dao.getNotes().size)
        dao.deleteAll()
        assertTrue(dao.getNotes().isEmpty())
    }
}
