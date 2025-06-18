package com.example.multi

import java.time.LocalDate

/** Model representing the snapshot of a weekly goal at the end of a week. */
data class WeeklyRecord(
    var id: Long = 0L,
    val header: String,
    val frequency: Int,
    val done: Int,
    val weekStart: String,
    val weekEnd: String
) {
    val completed: Boolean
        get() = done >= frequency

    fun startDate(): LocalDate = LocalDate.parse(weekStart)
    fun endDate(): LocalDate = LocalDate.parse(weekEnd)
}
