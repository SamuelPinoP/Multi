package com.example.multi.util

import com.example.multi.Event
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private val dayMap = mapOf(
    "sunday" to DayOfWeek.SUNDAY,
    "monday" to DayOfWeek.MONDAY,
    "tuesday" to DayOfWeek.TUESDAY,
    "wednesday" to DayOfWeek.WEDNESDAY,
    "thursday" to DayOfWeek.THURSDAY,
    "friday" to DayOfWeek.FRIDAY,
    "saturday" to DayOfWeek.SATURDAY
)

private data class Recurrence(val days: List<DayOfWeek>, val interval: Int)

private fun parseRecurrence(text: String): Recurrence? {
    var remaining = text.trim()
    var interval = 1
    if (remaining.startsWith("Every other", ignoreCase = true)) {
        interval = 2
        remaining = remaining.substringAfter("Every other", "").trim()
    } else if (remaining.startsWith("Every", ignoreCase = true)) {
        interval = 1
        remaining = remaining.substringAfter("Every", "").trim()
    }
    remaining = remaining.replace(" and ", ", ")
    val days = remaining.split(',').mapNotNull { name ->
        dayMap[name.trim().lowercase()]
    }
    return if (days.isNotEmpty()) Recurrence(days, interval) else null
}

/** Return true if this event occurs on the given [date]. */
fun Event.occursOn(date: LocalDate): Boolean {
    val str = this.date ?: return false
    // direct date match
    try {
        if (LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE) == date) {
            return true
        }
    } catch (_: DateTimeParseException) {
        // not an ISO date
    }
    val rec = parseRecurrence(str) ?: return false
    if (date.dayOfWeek !in rec.days) return false
    if (rec.interval <= 1) return true
    // align parity to a fixed reference Monday
    val ref = LocalDate.of(1970, 1, 5) // Monday
    val weeksBetween = ChronoUnit.WEEKS.between(ref, date)
    return weeksBetween % rec.interval == 0L
}
