package com.example.multi.util

import android.content.Context
import com.example.multi.Note
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

/**
 * Creates a DOCX file for the given note in the application's cache directory.
 * Returns the generated file instance.
 */
fun createNoteDocx(context: Context, note: Note): File {
    val document = XWPFDocument()

    val headerText = note.header.ifBlank { "Note" }
    document.createParagraph().createRun().apply {
        isBold = true
        fontSize = 20
        setText(headerText)
    }

    document.createParagraph().createRun().setText(note.content)

    val fileName = headerText.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".docx"
    val file = File(context.cacheDir, fileName)
    FileOutputStream(file).use { output ->
        document.write(output)
    }
    return file
}
