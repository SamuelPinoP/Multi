package com.example.multi.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY id")
    fun getEvents(): Flow<List<Event>>

    @Insert
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)
}
