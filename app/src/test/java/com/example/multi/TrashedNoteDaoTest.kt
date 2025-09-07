package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.multi.data.EventDatabase
import com.example.multi.data.NoteDao
import com.example.multi.data.NoteEntity
import com.example.multi.data.TrashedNoteDao
import com.example.multi.data.TrashedNoteEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class TrashedNoteDaoTest {
    private lateinit var db: EventDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var trashedDao: TrashedNoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java).build()
        noteDao = db.noteDao()
        trashedDao = db.trashedNoteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deleteAllClearsOnlyTrashed() = runBlocking {
        noteDao.insert(NoteEntity(header = "h", content = "c", created = 0L, lastOpened = 0L))
        trashedDao.insert(TrashedNoteEntity(header = "t1", content = "c1", created = 0L, deleted = 0L))
        trashedDao.insert(TrashedNoteEntity(header = "t2", content = "c2", created = 0L, deleted = 0L))

        assertEquals(2, trashedDao.getNotes().size)
        trashedDao.deleteAll()
        assertTrue(trashedDao.getNotes().isEmpty())
        assertEquals(1, noteDao.getNotes().size)
    }
}
