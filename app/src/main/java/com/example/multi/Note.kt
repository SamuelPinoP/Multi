package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var content: String,
    var created: Long = System.currentTimeMillis(),
    /** Font size for this note in sp. */
    var fontSize: Int = 20
)
