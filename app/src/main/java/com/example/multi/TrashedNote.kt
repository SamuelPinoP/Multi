package com.example.multi

/** Model representing a note moved to the trash bin. */
data class TrashedNote(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var created: Long,
    var deleted: Long = System.currentTimeMillis(),
    var attachmentUri: String? = null,
    var color: Int = 0xFF000000.toInt()
)
