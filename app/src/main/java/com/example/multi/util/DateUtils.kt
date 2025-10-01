package com.example.multi.util

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toDateString(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("M/d/yyyy"))
} else {
    val fmt = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
    fmt.format(Date(this))
}

private val dayNames = listOf(
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
)

private val calendarDays = listOf(
    Calendar.SUNDAY,
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.WEDNESDAY,
    Calendar.THURSDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY
)

/**
 * Returns a human-readable summary of the selected days, taking into account the repeat option.
 */
fun formatSelectedDays(dayChecks: List<Boolean>, repeatOption: String?): String? {
    val selectedNames = dayNames.filterIndexed { index, _ -> dayChecks.getOrNull(index) == true }
    if (selectedNames.isEmpty()) return null

    val prefix = when (repeatOption) {
        "Every" -> "Every"
        "Every other" -> "Every other"
        else -> ""
    }

    val dayString = when (selectedNames.size) {
        1 -> selectedNames.first()
        2 -> "${selectedNames[0]} and ${selectedNames[1]}"
        else -> selectedNames.dropLast(1).joinToString(", ") + " and " + selectedNames.last()
    }

    return if (prefix.isNotEmpty()) "$prefix $dayString" else dayString
}

/**
 * Calculates the next upcoming calendar date (yyyy-MM-dd) for the selected days.
 * Always returns a future date (never today).
 */
fun getNextDateForSelections(dayChecks: List<Boolean>, from: Calendar = Calendar.getInstance()): String? {
    val selectedIndices = dayChecks.mapIndexedNotNull { index, selected -> if (selected) index else null }
    if (selectedIndices.isEmpty()) return null

    val base = (from.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var nextDate: Calendar? = null
    for (index in selectedIndices) {
        val candidate = (base.clone() as Calendar).apply {
            val currentDay = get(Calendar.DAY_OF_WEEK)
            val targetDay = calendarDays.getOrNull(index) ?: Calendar.SUNDAY
            var daysAhead = (targetDay - currentDay + 7) % 7
            if (daysAhead <= 0) {
                daysAhead += 7
            }
            add(Calendar.DAY_OF_YEAR, daysAhead)
        }
        if (nextDate == null || candidate.before(nextDate)) {
            nextDate = candidate
        }
    }

    return nextDate?.toIsoDateString()
}

/** Converts a [Calendar] instance into a yyyy-MM-dd formatted string. */
fun Calendar.toIsoDateString(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Instant.ofEpochMilli(timeInMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
} else {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fmt.format(Date(timeInMillis))
}
