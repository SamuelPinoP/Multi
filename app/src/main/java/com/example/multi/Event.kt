package com.example.multi

/**
 * Model representing a single event entry with an optional date and notification time.
 */
data class Event(
    var id: Long = 0L,
    var title: String,
    var description: String,
    var date: String? = null,
    var address: String? = null,
    var notificationHour: Int? = null,  // Hour for notification (0-23)
    var notificationMinute: Int? = null, // Minute for notification (0-59)
    var notificationEnabled: Boolean = false, // Whether notification is enabled for this event
    var attachedNoteId: Long? = null // ID of note attached to this event, if any
) {
    /**
     * Returns a formatted string representation of the notification time.
     * @return Formatted time string (HH:MM) or null if notification is not set
     */
    fun getFormattedNotificationTime(): String? {
        return if (notificationEnabled && notificationHour != null && notificationMinute != null) {
            String.format("%02d:%02d", notificationHour, notificationMinute)
        } else {
            null
        }
    }

    /**
     * Sets the notification time for this event.
     * @param hour Hour in 24-hour format (0-23)
     * @param minute Minute (0-59)
     */
    fun setNotificationTime(hour: Int, minute: Int) {
        notificationHour = hour
        notificationMinute = minute
        notificationEnabled = true
    }

    /**
     * Disables notification for this event.
     */
    fun disableNotification() {
        notificationEnabled = false
        notificationHour = null
        notificationMinute = null
    }

    /**
     * Checks if this event has a valid notification time set.
     * @return true if notification is enabled and time is valid
     */
    fun hasValidNotificationTime(): Boolean {
        return notificationEnabled &&
                notificationHour != null &&
                notificationMinute != null &&
                notificationHour in 0..23 &&
                notificationMinute in 0..59
    }
}

