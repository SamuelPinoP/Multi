package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var created: Long = System.currentTimeMillis(),
    var lastOpened: Long = System.currentTimeMillis(),
    /** Vertical scroll offset when the note was last closed. */
    var scrollPos: Int = 0
)
