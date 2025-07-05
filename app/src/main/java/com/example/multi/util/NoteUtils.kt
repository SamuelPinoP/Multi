package com.example.multi.util

import com.example.multi.Note

/**
 * Return a short share title using the first two words from the note's header or content.
 */
fun Note.shareTitle(): String {
    val source = when {
        header.isNotBlank() -> header
        content.isNotBlank() -> content
        else -> "Note"
    }
    return source.firstTwoWords().ifBlank { "Note" }
}
