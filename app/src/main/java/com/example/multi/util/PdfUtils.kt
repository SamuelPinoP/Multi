package com.example.multi.util

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.multi.Note
import java.io.File
import java.io.FileOutputStream

/** Utility to export a [Note] as a PDF document. */
fun Note.writeToPdf(context: Context): File {
    val fileName = "note_${id.takeIf { it != 0L } ?: System.currentTimeMillis()}.pdf"
    val file = File(context.cacheDir, fileName)
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    val headerPaint = TextPaint().apply {
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val bodyPaint = TextPaint().apply { textSize = 12f }
    val width = pageInfo.pageWidth - 80
    val headerLayout = StaticLayout.Builder.obtain(
        header.ifBlank { "Note" }, 0, header.length, headerPaint, width
    ).setAlignment(Layout.Alignment.ALIGN_CENTER).build()
    canvas.save()
    canvas.translate(40f, 40f)
    headerLayout.draw(canvas)
    canvas.restore()

    val bodyLayout = StaticLayout.Builder.obtain(
        content, 0, content.length, bodyPaint, width
    ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()
    canvas.save()
    canvas.translate(40f, 40f + headerLayout.height + 20f)
    bodyLayout.draw(canvas)
    canvas.restore()

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
