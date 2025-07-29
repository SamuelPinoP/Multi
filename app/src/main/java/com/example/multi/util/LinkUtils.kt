package com.example.multi.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

private val linkRegex = Regex("(https?://\\S+|multi://\\S+)")

fun String.isLinkOnly(): Boolean {
    val trimmed = trim()
    return linkRegex.matches(trimmed)
}

fun openLink(context: Context, url: String) {
    if (url.startsWith("multi://")) {
        when (url.removePrefix("multi://")) {
            "calendar" -> context.startActivity(Intent(context, com.example.multi.CalendarActivity::class.java))
            "events" -> context.startActivity(Intent(context, com.example.multi.EventsActivity::class.java))
            "notes" -> context.startActivity(Intent(context, com.example.multi.NotesActivity::class.java))
            else -> {}
        }
    } else {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

@Composable
fun LinkifyText(text: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val annotated = buildAnnotatedString {
        var last = 0
        for (match in linkRegex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1
            append(text.substring(last, start))
            val url = match.value
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(url)
            }
            pop()
            last = end
        }
        append(text.substring(last))
    }
    ClickableText(text = annotated, modifier = modifier, style = MaterialTheme.typography.bodyLarge) { pos ->
        annotated.getStringAnnotations("URL", pos, pos).firstOrNull()?.let { sa ->
            if (sa.item.startsWith("multi://")) {
                openLink(context, sa.item)
            } else {
                uriHandler.openUri(sa.item)
            }
        }
    }
}
