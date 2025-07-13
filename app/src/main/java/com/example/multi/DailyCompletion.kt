package com.example.multi

data class DailyCompletion(
    val goalId: Long,
    val goalHeader: String,
    val completionDate: String, // ISO date string
    val weekStart: String,
    val weekEnd: String
)

// Extension functions
fun DailyCompletion.toEntity() = DailyCompletionEntity(
    goalId = goalId,
    goalHeader = goalHeader,
    completionDate = completionDate,
    weekStart = weekStart,
    weekEnd = weekEnd
)

fun DailyCompletionEntity.toModel() = DailyCompletion(
    goalId = goalId,
    goalHeader = goalHeader,
    completionDate = completionDate,
    weekStart = weekStart,
    weekEnd = weekEnd
)