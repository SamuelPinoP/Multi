package com.example.multi

import com.example.multi.util.TextMetrics
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TextMetricsTest {
    @Test
    fun emptyString_returnsZero() {
        assertEquals(0, TextMetrics.wordCount(""))
    }

    @Test
    fun multipleSpacesAndNewlines_correctCount() {
        val text = "one  two\nthree"
        assertEquals(3, TextMetrics.wordCount(text))
    }

    @Test
    fun punctuationAndEmoji_notOverCounted() {
        val text = "Hello, world! 😊"
        assertEquals(2, TextMetrics.wordCount(text))
    }

    @Test
    fun url_notOverCounted() {
        val text = "https://example.com"
        assertEquals(3, TextMetrics.wordCount(text))
    }

    @Test
    fun nonEnglishLocale_supported() {
        val text = "Bonjour le monde"
        assertEquals(3, TextMetrics.wordCount(text, Locale.FRENCH))
    }
}
