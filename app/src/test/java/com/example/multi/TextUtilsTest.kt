package com.example.multi

import com.example.multi.util.capitalizeSentences
import org.junit.Assert.assertEquals
import org.junit.Test

class TextUtilsTest {
    @Test
    fun capitalizeSentences_capitalizesCorrectly() {
        val input = "hello. this is a test.\nnew line." 
        val expected = "Hello. This is a test.\nNew line."
        assertEquals(expected, input.capitalizeSentences())
    }
}
