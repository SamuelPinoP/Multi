package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.multi.data.EventDatabase
import com.example.multi.data.TrashedNoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    fun deleteAllClearsNotes() = runBlocking {
        val dao = db.trashedNoteDao()
        dao.insert(TrashedNoteEntity(header = "A", content = "", created = 0, deleted = 0))
        dao.insert(TrashedNoteEntity(header = "B", content = "", created = 0, deleted = 0))
        assertTrue(dao.getNotes().isNotEmpty())
        dao.deleteAll()
        assertTrue(dao.getNotes().isEmpty())
    }
}

