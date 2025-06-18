package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi

/** Model representing a completed week of a [WeeklyGoal]. */
data class WeeklyRecord(
    val id: Long = 0L,
    val header: String,
    val frequency: Int,
    val completed: Int,
    val weekStart: String,
    val weekEnd: String
)

@RequiresApi(Build.VERSION_CODES.O)
fun lastWeekRange(): Pair<java.time.LocalDate, java.time.LocalDate> {
    val today = java.time.LocalDate.now()
    val thisMonday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val lastMonday = thisMonday.minusWeeks(1)
    val lastSunday = lastMonday.plusDays(6)
    return lastMonday to lastSunday
}
