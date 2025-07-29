package com.example.multi.util

import android.util.Patterns
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.MaterialTheme

/** Build an [AnnotatedString] with URL annotations for all links. */
fun buildLinkAnnotatedString(text: String): AnnotatedString {
    val matcher = Patterns.WEB_URL.matcher(text)
    var lastIndex = 0
    return buildAnnotatedString {
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            append(text.substring(lastIndex, start))
            val url = text.substring(start, end)
            pushStringAnnotation("URL", url)
            pushStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline))
            append(url)
            pop()
            pop()
            lastIndex = end
        }
        append(text.substring(lastIndex))
    }
}

/** Display [text] with clickable links using Compose. */
@Composable
fun LinkifyText(text: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val annotated = buildLinkAnnotatedString(text)
    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    ) { offset ->
        annotated.getStringAnnotations("URL", offset, offset).firstOrNull()?.let {
            uriHandler.openUri(it.item)
        }
    }
}
