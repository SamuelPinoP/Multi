package com.example.multi

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteUtilsTest {
    @Test
    fun `displayHeader uses trimmed header when available`() {
        val note = Note(header = "Line1\nLine2\nLine3\nLine4", content = "Body")
        assertEquals("Line1\nLine2\nLine3", note.displayHeader())
    }

    @Test
    fun `displayHeader falls back to content when header blank`() {
        val note = Note(header = "", content = "A\nB\nC\nD")
        assertEquals("A\nB\nC", note.displayHeader())
    }
}
