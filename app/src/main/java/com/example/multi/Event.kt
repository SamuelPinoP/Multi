package com.example.multi

/**
 * Model representing a single event entry with an optional date.
 */
data class Event(
    var id: Long = 0L,
    var title: String,
    var description: String,
    var date: String? = null,
    var address: String? = null,
    /** Time of day to notify the user about this event, or null if disabled. */
    var notifyTime: String? = "11:00"
)
