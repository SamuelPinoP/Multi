package com.example.multi.util

import android.content.Context
import com.example.multi.Note
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

/** Utility to create Word documents for notes. */
fun createDocxFile(context: Context, note: Note): File {
    val file = File(context.cacheDir, "note_${System.currentTimeMillis()}.docx")
    XWPFDocument().use { doc ->
        // Header
        val headerParagraph = doc.createParagraph()
        val headerRun = headerParagraph.createRun()
        headerRun.isBold = true
        headerRun.fontSize = 20
        headerRun.setText(note.header.ifBlank { "Note" })
        // Body
        doc.createParagraph().createRun().setText(note.content)
        FileOutputStream(file).use { out ->
            doc.write(out)
        }
    }
    return file
}
