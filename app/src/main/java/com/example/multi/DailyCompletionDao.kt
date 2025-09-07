package com.example.multi

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

    @Query("SELECT COUNT(*) FROM daily_completions WHERE goalId = :goalId AND weekStart = :weekStart AND weekEnd = :weekEnd")
    suspend fun getCompletionCountForWeek(goalId: Long, weekStart: String, weekEnd: String): Int
}