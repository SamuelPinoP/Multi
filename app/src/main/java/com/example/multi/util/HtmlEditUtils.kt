package com.example.multi.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.EditText

/** Insert an image from [uri] at the current cursor position of [editText]. */
fun insertImageIntoEditText(editText: EditText, uri: Uri) {
    val drawable: Drawable = try {
        editText.context.contentResolver.openInputStream(uri)?.use { input ->
            Drawable.createFromStream(input, uri.toString())
        } ?: return
    } catch (e: Exception) {
        return
    }
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    val span = ImageSpan(drawable, uri.toString())
    val start = editText.selectionStart.coerceAtLeast(0)
    val end = editText.selectionEnd.coerceAtLeast(start)
    val text: Editable = editText.editableText
    text.replace(start, end, " ")
    text.setSpan(span, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}

/** Convert the current text of [editText] to an HTML string. */
fun getHtmlFromEditText(editText: EditText): String {
    return Html.toHtml(editText.text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
}

/** Set [html] into the [editText] using [UriImageGetter]. */
fun setHtmlToEditText(editText: EditText, html: String, context: Context) {
    val spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT, UriImageGetter(context), null)
    editText.setText(spanned)
}
