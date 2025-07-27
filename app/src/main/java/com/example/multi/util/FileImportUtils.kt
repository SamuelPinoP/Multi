package com.example.multi.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.ss.usermodel.WorkbookFactory

fun getFileName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment ?: "imported_file"
}

fun readTextFromUri(context: Context, uri: Uri): String {
    val mime = context.contentResolver.getType(uri) ?: ""
    context.contentResolver.openInputStream(uri)?.use { input ->
        return when {
            mime.startsWith("text/") -> input.bufferedReader().use { it.readText() }
            mime == "application/pdf" -> {
                PDDocument.load(input).use { doc ->
                    PDFTextStripper().getText(doc)
                }
            }
            mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                XWPFDocument(input).use { doc ->
                    doc.paragraphs.joinToString("\n") { it.text }
                }
            }
            mime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                WorkbookFactory.create(input).use { wb ->
                    val sb = StringBuilder()
                    for (i in 0 until wb.numberOfSheets) {
                        val sheet = wb.getSheetAt(i)
                        sheet.forEach { row ->
                            row.forEach { cell ->
                                sb.append(cell.toString()).append('\t')
                            }
                            sb.append('\n')
                        }
                        sb.append('\n')
                    }
                    sb.toString()
                }
            }
            else -> ""
        }
    }
    return ""
}
