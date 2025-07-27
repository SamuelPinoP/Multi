package com.example.multi.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

/**
 * Read the provided [Uri] and return a pair of file name and extracted text.
 * Unsupported formats return an empty string for the text content.
 */
fun readTextFromUri(context: Context, uri: Uri): Pair<String, String> {
    val contentResolver = context.contentResolver
    val mime = contentResolver.getType(uri) ?: ""
    var name = "Imported"
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index != -1 && cursor.moveToFirst()) {
            name = cursor.getString(index)
        }
    }
    val input = contentResolver.openInputStream(uri) ?: return name to ""
    input.use { stream ->
        val text = when {
            mime.startsWith("text/") -> stream.bufferedReader().use { it.readText() }
            mime == "application/pdf" -> {
                PDDocument.load(stream).use { doc -> PDFTextStripper().getText(doc) }
            }
            mime == "application/msword" -> {
                WordExtractor(HWPFDocument(stream)).use { it.text }
            }
            mime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                XWPFDocument(stream).use { doc -> XWPFWordExtractor(doc).text }
            }
            mime == "application/vnd.ms-excel" ||
            mime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
                val wb = WorkbookFactory.create(stream)
                val sb = StringBuilder()
                for (sheetIndex in 0 until wb.numberOfSheets) {
                    val sheet = wb.getSheetAt(sheetIndex)
                    for (row in sheet) {
                        for (cell in row) {
                            sb.append(cell.toString()).append('\t')
                        }
                        sb.append('\n')
                    }
                }
                wb.close()
                sb.toString()
            }
            else -> ""
        }
        return name to text
    }
}
