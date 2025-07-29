package com.example.multi.util

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.inlineContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable

private val imageRegex = "\\[img:(.+?)\\]".toRegex()

data class ImageText(val text: AnnotatedString, val inline: Map<String, InlineTextContent>)

fun parseImageText(raw: String): ImageText {
    val builder = AnnotatedString.Builder()
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    var last = 0
    var index = 0
    for (match in imageRegex.findAll(raw)) {
        val start = match.range.first
        if (start > last) {
            builder.append(raw.substring(last, start))
        }
        val id = "img$index"
        builder.appendInlineContent(id, "")
        val uri = match.groupValues[1]
        inlineContent[id] = InlineTextContent(
            Placeholder(120.dp, 120.dp, PlaceholderVerticalAlign.Center)
        ) {
            val context = LocalContext.current
            val bitmap = rememberBitmapFromUri(Uri.parse(uri))
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(120.dp))
            }
        }
        last = match.range.last + 1
        index++
    }
    if (last < raw.length) {
        builder.append(raw.substring(last))
    }
    return ImageText(builder.toAnnotatedString(), inlineContent)
}

@Composable
fun rememberBitmapFromUri(uri: Uri): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = loadBitmapFromUri(context, uri)
    }
    return bitmap
}

fun loadBitmapFromUri(context: android.content.Context, uri: Uri): ImageBitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 28) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source).asImageBitmap()
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri).asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}
