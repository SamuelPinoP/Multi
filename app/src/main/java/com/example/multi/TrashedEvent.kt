package com.example.multi

/** Model representing an event moved to the trash bin. */
data class TrashedEvent(
    var id: Long = 0L,
    var title: String,
    var description: String,
    var date: String?,
    var address: String? = null,
    var notifyTime: String? = "11:00",
    var deleted: Long = System.currentTimeMillis()
)
