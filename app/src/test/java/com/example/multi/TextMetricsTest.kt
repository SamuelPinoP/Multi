package com.example.multi

import com.example.multi.util.TextMetrics
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TextMetricsTest {
    @Test
    fun emptyString_hasZeroWords() {
        assertEquals(0, TextMetrics.wordCount(""))
    }

    @Test
    fun multipleSpacesAndNewlines_countedCorrectly() {
        val text = "Hello   world\nthis is   a test"
        assertEquals(6, TextMetrics.wordCount(text))
    }

    @Test
    fun urlsEmojisAndPunctuation_notOverCounted() {
        val text = "Visit https://example.com! 😊"
        assertEquals(4, TextMetrics.wordCount(text))
    }

    @Test
    fun nonEnglishLocale_supported() {
        val text = "你好，世界！"
        assertEquals(2, TextMetrics.wordCount(text, Locale.CHINESE))
    }
}
