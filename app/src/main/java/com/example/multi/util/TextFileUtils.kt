package com.example.multi.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.multi.Note
import java.io.File
import android.text.Html

/** Utility to export a [Note] as a plain text file. */
fun Note.writeToTxt(context: Context): File {
    val fileName = "note_${id.takeIf { it != 0L } ?: System.currentTimeMillis()}.txt"
    val file = File(context.cacheDir, fileName)
    file.writeText(buildString {
        val headerLine = header.ifBlank { "Note" }
        append(headerLine)
        val plain = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString()
        if (plain.isNotBlank()) {
            append('\n')
            append(plain)
        }
    })
    return file
}

/** Share this [Note] as a plain text file using a share intent. */
fun Note.shareAsTxt(context: Context) {
    val file = writeToTxt(context)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}

/** Share multiple [notes] as separate text files using a share intent. */
fun shareNotesAsTxt(notes: List<Note>, context: Context) {
    if (notes.isEmpty()) return
    val uris = notes.map {
        val file = it.writeToTxt(context)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "text/plain"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
