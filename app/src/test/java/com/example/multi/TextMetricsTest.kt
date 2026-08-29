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
    fun blankString_hasZeroWords() {
        assertEquals(0, TextMetrics.wordCount("   \n\t "))
    }

    @Test
    fun multipleSpacesAndNewlines_countedCorrectly() {
        val text = "Hello   world\nthis is   a test"
        assertEquals(6, TextMetrics.wordCount(text))
    }

    @Test
    fun url_countsAsSingleWord() {
        // "Visit" + the whole URL = 2. The trailing "!" and the emoji are not words.
        val text = "Visit https://example.com! 😊"
        assertEquals(2, TextMetrics.wordCount(text))
    }

    @Test
    fun emailAndWwwLink_eachCountAsOneWord() {
        val text = "Email me at sam@example.co.uk or visit www.example.com now"
        // Email, me, at, <email>, or, visit, <www link>, now
        assertEquals(8, TextMetrics.wordCount(text))
    }

    @Test
    fun punctuationOnly_hasZeroWords() {
        assertEquals(0, TextMetrics.wordCount("!!! ... ??? -- ,,"))
    }

    @Test
    fun nonEnglishLocale_supported() {
        val text = "你好，世界！"
        assertEquals(2, TextMetrics.wordCount(text, Locale.CHINESE))
    }
}
