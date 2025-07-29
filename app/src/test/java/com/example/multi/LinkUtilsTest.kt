package com.example.multi

import com.example.multi.util.buildLinkAnnotatedString
import org.junit.Test
import org.junit.Assert.*

class LinkUtilsTest {
    @Test
    fun buildLinkAnnotatedString_detectsUrls() {
        val text = "Check https://example.com now"
        val annotated = buildLinkAnnotatedString(text)
        val annotations = annotated.getStringAnnotations("URL", 7, 7)
        assertEquals(1, annotations.size)
        assertEquals("https://example.com", annotations[0].item)
    }
}
