package com.example.multi

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.multi.data.EventDatabase
import com.example.multi.data.RoomNotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomNotesRepositoryTest {

    private lateinit var db: EventDatabase
    private lateinit var repository: RoomNotesRepository
    private var clock = 1_000_000L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EventDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomNotesRepository(
            noteDao = db.noteDao(),
            trashedNoteDao = db.trashedNoteDao(),
            ioDispatcher = Dispatchers.Unconfined,
            now = { clock },
        )
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insert_thenRead_returnsNotesNewestFirst() = runBlocking {
        repository.insert(Note(header = "old", content = "a", created = 1, lastOpened = 1))
        repository.insert(Note(header = "new", content = "b", created = 2, lastOpened = 5))

        assertEquals(listOf("new", "old"), repository.getNotes().map { it.header })
    }

    @Test
    fun observeNotes_emitsCurrentContents() = runBlocking {
        repository.insert(Note(header = "only", content = "a", created = 1, lastOpened = 1))

        val observed = repository.observeNotes().first()

        assertEquals(listOf("only"), observed.map { it.header })
    }

    @Test
    fun moveToTrash_removesFromNotes_andCopiesIntoTrashWithTimestamp() = runBlocking {
        val id = repository.insert(Note(header = "doomed", content = "x", created = 1, lastOpened = 1))
        val stored = repository.getNotes().first { it.id == id }

        repository.moveToTrash(listOf(stored))

        assertTrue(repository.getNotes().isEmpty())
        val trashed = db.trashedNoteDao().getNotes()
        assertEquals(1, trashed.size)
        assertEquals("doomed", trashed.first().header)
        assertEquals(clock, trashed.first().deleted)
    }

    @Test
    fun purgeExpiredTrash_onlyDeletesEntriesOlderThanRetention() = runBlocking {
        val fresh = repository.insert(Note(header = "fresh", content = "1", created = 1, lastOpened = 1))
        val stale = repository.insert(Note(header = "stale", content = "2", created = 1, lastOpened = 1))

        // Trash "stale" 40 days ago, "fresh" just now.
        clock = 0L
        repository.moveToTrash(listOf(repository.getNotes().first { it.id == stale }))
        clock = 40L * 24 * 60 * 60 * 1000
        repository.moveToTrash(listOf(repository.getNotes().first { it.id == fresh }))

        repository.purgeExpiredTrash()

        val remaining = db.trashedNoteDao().getNotes()
        assertEquals(listOf("fresh"), remaining.map { it.header })
    }
}
