package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

/** Model representing a weekly goal entry. */
data class WeeklyGoal(
    var id: Long = 0L,
    var header: String,
    var frequency: Int,
    var remaining: Int = frequency,
    var lastCheckedDate: String? = null,
    var weekNumber: Int = currentWeek()
)

@RequiresApi(Build.VERSION_CODES.O)
fun currentWeek(): Int = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

@RequiresApi(Build.VERSION_CODES.O)
fun daysRemainingInWeek(): Int {
    val today = LocalDate.now()
    val nextSunday = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
    return ChronoUnit.DAYS.between(today, nextSunday).toInt() - 1
}
