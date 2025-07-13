package com.example.multi

import android.content.Context
import com.example.multi.DailyCompletion
import com.example.multi.data.EventDatabase
import com.example.multi.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

suspend fun saveGoalCompletion(
    context: Context,
    goalId: Long,
    goalHeader: String,
    completionDate: LocalDate
) {
    val dayOfWeek = completionDate.dayOfWeek.value % 7
    val weekStart = completionDate.minusDays(dayOfWeek.toLong())
    val weekEnd = weekStart.plusDays(6)

    val completion = DailyCompletion(
        goalId = goalId,
        goalHeader = goalHeader,
        completionDate = completionDate.toString(),
        weekStart = weekStart.toString(),
        weekEnd = weekEnd.toString()
    )

    val dao = EventDatabase.getInstance(context).dailyCompletionDao()
    withContext(Dispatchers.IO) {
        dao.insert(completion.toEntity())
    }
}

suspend fun removeGoalCompletion(
    context: Context,
    goalId: Long,
    completionDate: LocalDate
) {
    val dao = EventDatabase.getInstance(context).dailyCompletionDao()
    withContext(Dispatchers.IO) {
        dao.deleteCompletion(goalId, completionDate.toString())
    }
}