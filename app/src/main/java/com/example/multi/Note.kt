package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var content: String,
    var textSize: Float = 20f,
    var created: Long = System.currentTimeMillis()
)
