package com.example.multi

/**
 * Model representing a single event entry with an optional date.
 */
const val DEFAULT_NOTIFY_TIME = "11:00 AM"

data class Event(
    var id: Long = 0L,
    var title: String,
    var description: String,
    var date: String? = null,
    var address: String? = null,
    /** Notification time in hh:mm a format, or null if disabled. */
    var notifyTime: String? = DEFAULT_NOTIFY_TIME
)
