package com.example.multi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Event::class], version = 1)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile private var INSTANCE: EventDatabase? = null

        fun get(context: Context): EventDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "events.db"
                ).build().also { INSTANCE = it }
            }
    }
}
