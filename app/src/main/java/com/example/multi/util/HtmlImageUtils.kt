package com.example.multi.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Html

/** ImageGetter that loads images from content URIs for Html.fromHtml. */
class UriImageGetter(private val context: Context) : Html.ImageGetter {
    override fun getDrawable(source: String?): Drawable {
        val uri = source?.let { Uri.parse(it) } ?: return ColorDrawable(0x00000000)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                Drawable.createFromStream(input, uri.toString())?.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                }
            } ?: ColorDrawable(0x00000000).apply { setBounds(0, 0, 1, 1) }
        } catch (e: Exception) {
            ColorDrawable(0x00000000).apply { setBounds(0, 0, 1, 1) }
        }
    }
}
