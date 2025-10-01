package com.example.multi.util

import android.os.Build
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Long.toDateString(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("M/d/yyyy"))
} else {
    val fmt = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
    fmt.format(Date(this))
}

private val dayOfWeekMapping = listOf(
    Calendar.SUNDAY,
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.WEDNESDAY,
    Calendar.THURSDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY
)

/**
 * Returns the next calendar occurrence for the given day index (0 = Sunday, 6 = Saturday).
 */
private fun nextCalendarForDay(index: Int, from: Calendar = Calendar.getInstance()): Calendar {
    val calendar = from.clone() as Calendar
    val targetDay = dayOfWeekMapping.getOrNull(index) ?: Calendar.SUNDAY
    val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
    var daysUntil = targetDay - currentDay
    if (daysUntil <= 0) {
        daysUntil += 7
    }
    calendar.add(Calendar.DAY_OF_YEAR, daysUntil)
    return calendar
}

/**
 * Calculates the soonest upcoming date based on a list of weekly selections.
 *
 * @param selections Boolean flags for each day of week starting with Sunday.
 * @param pattern Output date format, defaults to ISO `yyyy-MM-dd`.
 * @return Formatted date string for the soonest upcoming selection or null if none are selected.
 */
fun nextDateForSelectedDays(
    selections: List<Boolean>,
    pattern: String = "yyyy-MM-dd",
): String? {
    if (selections.isEmpty()) return null
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    val now = Calendar.getInstance()
    var earliest: Calendar? = null
    selections.forEachIndexed { index, isChecked ->
        if (isChecked) {
            val candidate = nextCalendarForDay(index, now)
            if (earliest == null || candidate.before(earliest)) {
                earliest = candidate
            }
        }
    }
    return earliest?.let { formatter.format(it.time) }
}
