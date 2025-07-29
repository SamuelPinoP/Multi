package com.example.multi.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration

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
        if (c == '.' || c == '!' || c == '?' || c == ';' || c == '\n') {
            capitalizeNext = true
        }
    }
    return result.toString()
}

/** Convert this string into an [AnnotatedString] with link annotations. */
fun String.toAnnotatedStringWithLinks(): AnnotatedString {
    val regex = "(https?://\\S+|app://\\S+)".toRegex()
    return buildAnnotatedString {
        var currentIndex = 0
        regex.findAll(this@toAnnotatedStringWithLinks).forEach { match ->
            val range = match.range
            if (range.first > currentIndex) {
                append(this@toAnnotatedStringWithLinks.substring(currentIndex, range.first))
            }
            val url = match.value
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(url)
            }
            pop()
            currentIndex = range.last + 1
        }
        if (currentIndex < this@toAnnotatedStringWithLinks.length) {
            append(this@toAnnotatedStringWithLinks.substring(currentIndex))
        }
    }
}
