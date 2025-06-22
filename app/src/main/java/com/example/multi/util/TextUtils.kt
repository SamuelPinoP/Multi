package com.example.multi.util

/** Capitalize the first letter of the text and after sentence-ending punctuation. */
fun String.capitalizeSentences(): String {
    val result = StringBuilder(length)
    var capitalizeNext = true
    for (c in this) {
        val ch = if (capitalizeNext && c.isLetter()) {
            capitalizeNext = false
            c.uppercaseChar()
        } else {
            c
        }
        result.append(ch)
        if (c == '.' || c == '!' || c == '?' || c == '\n') {
            capitalizeNext = true
        }
    }
    return result.toString()
}
