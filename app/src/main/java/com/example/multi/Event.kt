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
    /** Time of day for notification in HH:mm format. Null to disable. */
    var notifyTime: String? = "11:00"
)
