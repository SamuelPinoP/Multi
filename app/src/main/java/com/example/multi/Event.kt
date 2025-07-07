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

private val dayRegex = "(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)".toRegex()

/**
 * Returns true if this event occurs on the given [date].
 * Supports single dates (yyyy-MM-dd), "Every <day>" and
 * "Every other <day>" patterns as saved by [EventDialog].
 */
fun Event.occursOn(date: java.time.LocalDate): Boolean {
    val info = date
    val d = this.date ?: return false
    return when {
        d.startsWith("Every other") -> {
            val days = dayRegex.findAll(d).map {
                java.time.DayOfWeek.valueOf(it.value.uppercase(java.util.Locale.US))
            }.toSet()
            if (info.dayOfWeek !in days) return false
            val week = info.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            week % 2 == 0
        }
        d.startsWith("Every") -> {
            val days = dayRegex.findAll(d).map {
                java.time.DayOfWeek.valueOf(it.value.uppercase(java.util.Locale.US))
            }.toSet()
            info.dayOfWeek in days
        }
        else -> {
            try {
                java.time.LocalDate.parse(d) == info
            } catch (_: Exception) {
                false
            }
        }
    }
}
