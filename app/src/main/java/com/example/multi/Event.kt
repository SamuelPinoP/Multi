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
    var reminderEnabled: Boolean = true,
    /** Reminder time in HH:mm format */
    var reminderTime: String = "11:00"
)
