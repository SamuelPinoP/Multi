package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.multi.data.TrashedNoteEntity

class TrashedNoteDaoTest {
    private lateinit var db: com.example.multi.data.EventDatabase
    private lateinit var dao: com.example.multi.data.TrashedNoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, com.example.multi.data.EventDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.trashedNoteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deleteAll_removes_all_entries() = runBlocking {
        dao.insert(TrashedNoteEntity(header = "a", content = "b", created = 0L, deleted = 0L))
        dao.insert(TrashedNoteEntity(header = "c", content = "d", created = 0L, deleted = 0L))
        assertEquals(2, dao.getNotes().size)
        dao.deleteAll()
        assertTrue(dao.getNotes().isEmpty())
    }
}
