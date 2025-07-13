package com.example.multi.data

import androidx.room.*

@Entity(tableName = "daily_completions")
data class DailyCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val goalHeader: String,
    val completionDate: String,
    val weekStart: String,
    val weekEnd: String
)