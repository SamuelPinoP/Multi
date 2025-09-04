package com.example.multi.util

import java.text.BreakIterator
import java.util.Locale

object TextMetrics {
    fun wordCount(text: String, locale: Locale = Locale.getDefault()): Int {
        val iterator = BreakIterator.getWordInstance(locale)
        iterator.setText(text)
        var count = 0
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            val token = text.substring(start, end)
            if (token.any { it.isLetterOrDigit() }) {
                count++
            }
            start = end
            end = iterator.next()
        }
        return count
    }
}
