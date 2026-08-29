package com.example.multi.util

import java.text.BreakIterator
import java.util.Locale

/** Utility object for computing text metrics such as word count. */
object TextMetrics {

    /**
     * URLs and e-mail addresses. Matched and counted as a single "word" before
     * the [BreakIterator] pass, otherwise `https://example.com` would inflate
     * the count (scheme, host and TLD each scored separately).
     */
    private val ATOMIC_TOKEN = Regex(
        """(?:[a-z][a-z0-9+.-]*://\S+)""" +           // scheme://...
            """|(?:www\.\S+)""" +                      // www.something
            """|(?:[^\s@]+@[^\s@]+\.[^\s@]+)""",       // email@host.tld
        RegexOption.IGNORE_CASE,
    )

    /**
     * Count words in [text].
     *
     * A "word" is any token that contains at least one letter or digit, with
     * URLs and e-mail addresses collapsed to a single token first. A
     * locale-aware [BreakIterator] handles the remainder so scripts without
     * spaces (e.g. Chinese) are still segmented correctly.
     */
    fun wordCount(text: String, locale: Locale = Locale.getDefault()): Int {
        if (text.isBlank()) return 0

        var atomicCount = 0
        val remainder = ATOMIC_TOKEN.replace(text) { match ->
            if (match.value.any { it.isLetterOrDigit() }) atomicCount++
            " "
        }

        return atomicCount + breakIteratorWordCount(remainder, locale)
    }

    private fun breakIteratorWordCount(text: String, locale: Locale): Int {
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
