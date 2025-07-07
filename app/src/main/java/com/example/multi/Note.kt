package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var address: String = "",
    var created: Long = System.currentTimeMillis(),
    var lastOpened: Long = System.currentTimeMillis(),
    var scroll: Int = 0,
    var cursor: Int = 0
)
