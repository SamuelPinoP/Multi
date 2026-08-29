package com.example.multi.di

import android.content.Context
import com.example.multi.data.EventDatabase
import com.example.multi.data.NotesRepository
import com.example.multi.data.RoomNotesRepository

/**
 * Minimal dependency-injection container.
 *
 * The app is small enough that a hand-rolled service locator buys the main
 * benefit of a DI framework — a single wiring point and swappable
 * implementations in tests — without the Hilt/Dagger build cost. Overrides
 * (see [setNotesRepository]) let instrumentation tests substitute fakes.
 */
object ServiceLocator {

    @Volatile
    private var notesRepositoryOverride: NotesRepository? = null

    @Volatile
    private var notesRepositoryInstance: NotesRepository? = null

    fun provideNotesRepository(context: Context): NotesRepository {
        notesRepositoryOverride?.let { return it }
        return notesRepositoryInstance ?: synchronized(this) {
            notesRepositoryInstance ?: createNotesRepository(context).also {
                notesRepositoryInstance = it
            }
        }
    }

    private fun createNotesRepository(context: Context): NotesRepository {
        val db = EventDatabase.getInstance(context.applicationContext)
        return RoomNotesRepository(db.noteDao(), db.trashedNoteDao())
    }

    /** Test hook: force a specific repository implementation. */
    fun setNotesRepository(repository: NotesRepository?) {
        notesRepositoryOverride = repository
    }

    /** Test hook: drop cached singletons so each test starts clean. */
    fun reset() {
        notesRepositoryOverride = null
        notesRepositoryInstance = null
    }
}
