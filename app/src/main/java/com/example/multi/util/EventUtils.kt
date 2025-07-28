package com.example.multi.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeParseException
import com.example.multi.Event

/** Return the set of [DayOfWeek] mentioned in this string, or null if it's not
 *  a recurring pattern. Recognizes day names like "Monday" or "Tue" anywhere
 *  in the text. */
fun String?.recurringDays(): Set<DayOfWeek>? {
    if (this == null) return null
    val trimmed = trim()
    // If it parses as a date, it's not a recurring description
    try {
        LocalDate.parse(trimmed)
        return null
    } catch (_: DateTimeParseException) {
        // not a plain date
    }
    val lower = trimmed.lowercase()
    val result = mutableSetOf<DayOfWeek>()
    DayOfWeek.entries.forEach { dow ->
        if (lower.contains(dow.name.lowercase().take(3))) {
            result.add(dow)
        }
    }
    return if (result.isEmpty()) null else result
}

/** Check if this [Event] applies to the given [date]. */
fun Event.occursOn(date: LocalDate): Boolean {
    val info = date?.let { this.date } ?: return false
    val recurring = info.recurringDays()
    return if (recurring != null) {
        date.dayOfWeek in recurring
    } else {
        info == date.toString()
    }
}
