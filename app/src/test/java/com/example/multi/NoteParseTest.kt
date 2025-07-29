package com.example.multi

import org.junit.Test
import org.junit.Assert.*

class NoteParseTest {
    @Test
    fun parse_blocks_with_image() {
        val text = "Hello\n![](content://img)\nWorld"
        val blocks = parseNoteBlocks(text)
        assertEquals(3, blocks.size)
        assertEquals("Hello\n", blocks[0].text)
        assertEquals("content://img", blocks[1].imageUri)
        assertEquals("\nWorld", blocks[2].text)
    }
}
