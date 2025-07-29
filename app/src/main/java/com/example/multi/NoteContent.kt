package com.example.multi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class NoteElement {
    data class Text(val text: String) : NoteElement()
    data class Image(val uri: Uri) : NoteElement()
}

private val imageRegex = Regex("\\[img:(.+?)\\]")

fun parseNoteContent(text: String): List<NoteElement> {
    val result = mutableListOf<NoteElement>()
    var current = 0
    imageRegex.findAll(text).forEach { match ->
        if (match.range.first > current) {
            result += NoteElement.Text(text.substring(current, match.range.first))
        }
        val uriString = match.groupValues[1]
        result += NoteElement.Image(Uri.parse(uriString))
        current = match.range.last + 1
    }
    if (current < text.length) {
        result += NoteElement.Text(text.substring(current))
    }
    return result
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ImageFromUri(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) { loadBitmapFromUri(context, uri) }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
        )
    }
}

@Composable
fun RenderNoteContent(header: String, content: String) {
    val pieces = remember(content) { parseNoteContent(content) }
    Column(modifier = Modifier.padding(16.dp)) {
        if (header.isNotBlank()) {
            Text(
                text = header,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        pieces.forEach { piece ->
            when (piece) {
                is NoteElement.Text -> Text(
                    text = piece.text,
                    style = MaterialTheme.typography.bodyLarge
                )
                is NoteElement.Image -> ImageFromUri(
                    piece.uri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}
