package com.example.multi

/**
 * Model representing a single event entry with an optional date.
 */
data class Event(
    var id: Long = 0L,
    var title: String,
    var description: String,
    var date: String? = null
)

/** Helper for weekly recurring events. */
data class Recurrence(val interval: Int, val days: List<java.time.DayOfWeek>)

private fun parseRecurrence(str: String): Recurrence? {
    var text = str.trim()
    var interval = 1
    val lower = text.lowercase(java.util.Locale.getDefault())
    when {
        lower.startsWith("every other ") -> {
            interval = 2
            text = text.substring(12)
        }
        lower.startsWith("every ") -> {
            text = text.substring(6)
        }
    }
    text = text.replace(" and ", ", ")
    val days = text.split(',').mapNotNull { name ->
        val n = name.trim()
        if (n.isEmpty()) null else {
            java.time.DayOfWeek.entries.firstOrNull {
                it.name.equals(n.uppercase(java.util.Locale.getDefault()), true)
            }
        }
    }
    return if (days.isNotEmpty()) Recurrence(interval, days) else null
}

/** Return true if the event occurs on the given [date]. */
fun Event.occursOn(date: java.time.LocalDate): Boolean {
    val raw = this.date ?: return false
    try {
        if (java.time.LocalDate.parse(raw) == date) return true
    } catch (_: java.time.format.DateTimeParseException) {
        // ignore, not a single date
    }
    val rec = parseRecurrence(raw) ?: return false
    if (date.dayOfWeek !in rec.days) return false
    if (rec.interval == 1) return true
    val week = date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    return week % rec.interval == 0
}
