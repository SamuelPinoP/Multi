package com.example.multi.data

import androidx.room.*

@Dao
interface DailyCompletionDao {
    @Query("SELECT * FROM daily_completions WHERE completionDate = :date")
    suspend fun getCompletionsForDate(date: String): List<DailyCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: DailyCompletionEntity)

    @Query("DELETE FROM daily_completions WHERE goalId = :goalId AND completionDate = :date")
    suspend fun deleteCompletion(goalId: Long, date: String)

    @Query("SELECT * FROM daily_completions WHERE completionDate BETWEEN :startDate AND :endDate")
    suspend fun getCompletionsInRange(startDate: String, endDate: String): List<DailyCompletionEntity>
}