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

private fun Calendar.toIsoDateString(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Instant.ofEpochMilli(timeInMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
} else {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fmt.format(Date(timeInMillis))
}

/**
 * Calculates the next calendar date (in yyyy-MM-dd format) that matches any of the
 * selected days of the week where Sunday = index 0 and Saturday = index 6.
 * Returns null when no days are selected.
 */
fun nextDateForSelectedDays(
    selections: List<Boolean>,
    reference: Calendar = Calendar.getInstance()
): String? {
    val selectedIndices = selections.mapIndexedNotNull { index, isChecked ->
        if (isChecked) index else null
    }
    if (selectedIndices.isEmpty()) return null

    val currentDayIndex = reference.get(Calendar.DAY_OF_WEEK) - 1
    val base = (reference.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var nextMatch: Calendar? = null
    for (targetIndex in selectedIndices) {
        var daysAhead = (targetIndex - currentDayIndex + 7) % 7
        if (daysAhead == 0) {
            daysAhead = 7
        }
        val candidate = (base.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, daysAhead)
        }
        if (nextMatch == null || candidate.before(nextMatch)) {
            nextMatch = candidate
        }
    }

    return nextMatch?.toIsoDateString()
}
