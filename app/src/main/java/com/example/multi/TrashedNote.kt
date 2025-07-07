package com.example.multi

/** Model representing a note moved to the trash bin. */
data class TrashedNote(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var created: Long,
    var address: String = "",
    var deleted: Long = System.currentTimeMillis()
)
