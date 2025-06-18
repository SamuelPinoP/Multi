package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

/** Model representing a weekly recurring goal. */
@RequiresApi(Build.VERSION_CODES.O)
fun currentWeek(): Int = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

@RequiresApi(Build.VERSION_CODES.O)
fun daysRemainingInWeek(): Int {
    val today = LocalDate.now()
    val nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    return ChronoUnit.DAYS.between(today, nextMonday).toInt() - 1
}

@RequiresApi(Build.VERSION_CODES.O)
fun lastWeekRange(): Pair<LocalDate, LocalDate> {
    val currentMonday = LocalDate.now().with(DayOfWeek.MONDAY)
    val start = currentMonday.minusWeeks(1)
    val end = start.plusDays(6)
    return start to end
}

data class WeeklyGoalRecord(
    val id: Long = 0L,
    val header: String,
    val frequency: Int,
    val completed: Int,
    val startDate: String,
    val endDate: String
)

data class WeeklyGoal(
    var id: Long = 0L,
    var header: String,
    var frequency: Int,
    var remaining: Int = frequency,
    var lastCheckedDate: String? = null,
    var weekNumber: Int = currentWeek()
)
