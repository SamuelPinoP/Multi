package com.example.multi

import com.example.multi.util.capitalizeSentences
import org.junit.Assert.assertEquals
import org.junit.Test

class TextUtilsTest {

    @Test
    fun capitalizesFirstLetter() {
        assertEquals("Hello world", "hello world".capitalizeSentences())
    }

    @Test
    fun capitalizesAfterSentenceEndingPunctuation() {
        assertEquals(
            "One thing. Two things! Three things? Four.",
            "one thing. two things! three things? four.".capitalizeSentences(),
        )
    }

    @Test
    fun capitalizesAfterNewline() {
        assertEquals("First line\nSecond line", "first line\nsecond line".capitalizeSentences())
    }

    @Test
    fun capitalizesFirstLetterEvenWhenPrecededByDigits() {
        assertEquals("3 Apples. NASA rocks.", "3 apples. NASA rocks.".capitalizeSentences())
    }

    @Test
    fun emptyStringStaysEmpty() {
        assertEquals("", "".capitalizeSentences())
    }
}
