package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var created: Long = System.currentTimeMillis()
)

/** Returns the header trimmed to a maximum of three lines. */
fun Note.trimmedHeader(): String = header.lines().take(3).joinToString("\n")

/**
 * Provides the text to display for this note's header. If the trimmed header is
 * blank, the first three lines of the note content are returned instead.
 */
fun Note.displayHeader(): String {
    val trimmed = trimmedHeader()
    return if (trimmed.isNotBlank()) trimmed else content.lines().take(3).joinToString("\n")
}
