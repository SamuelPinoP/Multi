package com.example.multi.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image

sealed interface NotePart {
    data class TextPart(val text: String) : NotePart
    data class ImagePart(val uri: String) : NotePart
}

private val imageRegex = Regex("\\[img:(.+?)\\]")

fun parseNoteContent(content: String): List<NotePart> {
    val parts = mutableListOf<NotePart>()
    var lastIndex = 0
    for (match in imageRegex.findAll(content)) {
        val start = match.range.first
        if (start > lastIndex) {
            parts.add(NotePart.TextPart(content.substring(lastIndex, start)))
        }
        parts.add(NotePart.ImagePart(match.groupValues[1]))
        lastIndex = match.range.last + 1
    }
    if (lastIndex < content.length) {
        parts.add(NotePart.TextPart(content.substring(lastIndex)))
    }
    return parts
}

@Composable
fun NotePreview(parts: List<NotePart>, textSize: Int, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.verticalScroll(scrollState)) {
        parts.forEach { part ->
            when (part) {
                is NotePart.TextPart -> {
                    if (part.text.isNotBlank()) {
                        Text(
                            text = part.text,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp)
                        )
                    }
                }
                is NotePart.ImagePart -> {
                    Image(
                        painter = rememberAsyncImagePainter(part.uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
