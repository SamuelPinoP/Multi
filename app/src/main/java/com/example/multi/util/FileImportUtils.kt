package com.example.multi.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.multi.Note
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument

/** Return the display name for the given [uri]. */
fun ContentResolver.getFileName(uri: Uri): String {
    var name = uri.lastPathSegment ?: "import"
    query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index != -1 && cursor.moveToFirst()) {
            name = cursor.getString(index)
        }
    }
    return name
}

/**
 * Create a [Note] from the file referenced by [uri].
 * Text and common Office formats are converted to plain text when possible.
 */
fun Context.importNoteFromUri(uri: Uri): Note {
    val resolver = contentResolver
    val mime = resolver.getType(uri) ?: ""
    val name = resolver.getFileName(uri)
    val header = name.substringBeforeLast('.')

    val content = when {
        mime == "text/plain" ->
            resolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
        mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
            resolver.openInputStream(uri)?.use { input ->
                val doc = XWPFDocument(input)
                doc.paragraphs.joinToString("\n") { it.text }
            } ?: ""
        mime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
            resolver.openInputStream(uri)?.use { input ->
                val workbook = XSSFWorkbook(input)
                val sheet = workbook.getSheetAt(0)
                val sb = StringBuilder()
                for (row in sheet) {
                    val line = row.joinToString("\t") { cell -> cell.toString() }
                    sb.append(line).append('\n')
                }
                workbook.close()
                sb.toString()
            } ?: ""
        mime == "application/pdf" -> "[PDF: $name]"
        mime.startsWith("image/") -> "[Image: $name]"
        else -> "[File: $name]"
    }

    return Note(header = header, content = content)
}
