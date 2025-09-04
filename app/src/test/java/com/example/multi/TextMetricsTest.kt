package com.example.multi

import com.example.multi.util.TextMetrics
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TextMetricsTest {
    @Test
    fun emptyString() {
        assertEquals(0, TextMetrics.wordCount(""))
    }

    @Test
    fun multipleSpacesAndNewlines() {
        val text = "Hello   world\n\nthis is   a test"
        assertEquals(6, TextMetrics.wordCount(text))
    }

    @Test
    fun urlEmojiPunctuationNotOverCounted() {
        val text = "Hello, world! 👋 Visit https://example.com"
        assertEquals(6, TextMetrics.wordCount(text))
    }

    @Test
    fun nonEnglishLocale() {
        val text = "你好 世界"
        assertEquals(2, TextMetrics.wordCount(text, Locale.CHINESE))
    }
}
