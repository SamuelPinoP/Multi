package com.example.multi.util

/**
 * Capitalize the first letter of each sentence in the string.
 * Sentences are determined by periods or line breaks.
 */
fun String.capitalizeSentences(): String {
    val builder = StringBuilder(length)
    var capitalizeNext = true
    for (ch in this) {
        if (capitalizeNext && ch.isLetter()) {
            builder.append(ch.uppercaseChar())
            capitalizeNext = false
        } else {
            builder.append(ch)
        }
        if (ch == '.' || ch == '\n') {
            capitalizeNext = true
        }
    }
    return builder.toString()
}
