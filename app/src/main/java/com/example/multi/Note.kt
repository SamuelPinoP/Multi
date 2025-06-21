package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var created: Long = System.currentTimeMillis(),
    /** Time when the note was moved to trash. Null if not deleted. */
    var deleted: Long? = null
)
