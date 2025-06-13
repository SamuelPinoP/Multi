package com.example.multi

import androidx.compose.runtime.Composable
import com.alamkanak.weekview.compose.WeekView
import com.alamkanak.weekview.compose.rememberWeekViewState
import java.time.DayOfWeek
import java.time.LocalDate

/** Returns the zero-based offset used by the calendar grid for this day. */
internal fun DayOfWeek.toCalendarOffset(): Int = (this.value + 6) % 7

/**
 * Displays a scrollable calendar using Android-Week-View. It starts at
 * January 2025 and allows navigating infinitely into the future.
 */
@Composable
fun CalendarView(date: LocalDate = LocalDate.of(2025, 1, 1)) {
    val state = rememberWeekViewState(
        initialDate = date,
        minDate = LocalDate.of(2025, 1, 1)
    )
    WeekView(state = state)
}
