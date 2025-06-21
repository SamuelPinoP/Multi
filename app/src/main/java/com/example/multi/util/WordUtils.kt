package com.example.multi.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.multi.Note
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

/** Utility to export a [Note] as a Word document. */
fun Note.writeToDocx(context: Context): File {
    val fileName = "note_${id.takeIf { it != 0L } ?: System.currentTimeMillis()}.docx"
    val file = File(context.cacheDir, fileName)
    val document = XWPFDocument()
    val headerPara = document.createParagraph()
    val headerRun = headerPara.createRun()
    headerRun.isBold = true
    headerRun.fontSize = 20
    headerRun.setText(header.ifBlank { "Note" })
    val bodyPara = document.createParagraph()
    val bodyRun = bodyPara.createRun()
    bodyRun.setText(content)
    FileOutputStream(file).use { document.write(it) }
    document.close()
    return file
}

/** Share this [Note] as a Word document using a share intent. */
fun Note.shareAsDocx(context: Context) {
    val file = writeToDocx(context)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
