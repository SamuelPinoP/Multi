package com.example.multi

/** Simple model representing a note entry. */
data class Note(
    var id: Long = 0L,
    var header: String = "",
    var content: String,
    var created: Long = System.currentTimeMillis()
)

/**
 * Returns the text that should be shown for this note's header. If the header
 * is blank the preview will consist of the first [maxLines] lines of the note
 * content. Only up to [maxLines] lines of text will be returned.
 */
fun Note.previewHeader(maxLines: Int = 3): String {
    val source = if (header.isNotBlank()) header else content
    return source.lines().take(maxLines).joinToString("\n")
}
