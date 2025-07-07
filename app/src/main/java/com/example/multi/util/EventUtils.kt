package com.example.multi.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields

/** Return true if the [eventDate] string represents an occurrence on the given [date]. */
fun eventMatchesDate(eventDate: String?, date: LocalDate): Boolean {
    eventDate ?: return false
    // Check ISO date format like 2024-01-05
    if (eventDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        return eventDate == date.toString()
    }

    var text = eventDate.trim()
    var everyOther = false
    if (text.startsWith("Every other ")) {
        everyOther = true
        text = text.removePrefix("Every other ").trim()
    } else if (text.startsWith("Every ")) {
        text = text.removePrefix("Every ").trim()
    }

    // Replace " and " with commas to split day names
    text = text.replace(" and ", ",")
    val dayNames = text.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (dayNames.isEmpty()) return false

    val daysOfWeek = dayNames.mapNotNull { name ->
        try {
            DayOfWeek.valueOf(name.uppercase())
        } catch (e: Exception) { null }
    }
    if (daysOfWeek.isEmpty()) return false

    if (date.dayOfWeek !in daysOfWeek) return false

    return if (everyOther) {
        val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        week % 2 == 0
    } else {
        true
    }
}
