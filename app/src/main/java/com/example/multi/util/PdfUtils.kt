package com.example.multi.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.multi.Note
import java.io.File
import java.io.FileOutputStream

/** Utility to export a [Note] as a PDF document. */
fun Note.writeToPdf(context: Context): File {
    val fileName = "note_${id.takeIf { it != 0L } ?: System.currentTimeMillis()}.pdf"
    val file = File(context.cacheDir, fileName)
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText(header.ifBlank { "Note" }, 40f, 50f, paint)
    paint.textSize = 16f
    paint.isFakeBoldText = false
    var y = 80f
    for (line in content.split('\n')) {
        canvas.drawText(line, 40f, y, paint)
        y += paint.textSize + 8f
    }
    document.finishPage(page)
    FileOutputStream(file).use { document.writeTo(it) }
    document.close()
    return file
}

/** Share this [Note] as a PDF document using a share intent. */
fun Note.shareAsPdf(context: Context) {
    val file = writeToPdf(context)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
