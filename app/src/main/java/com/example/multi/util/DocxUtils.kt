package com.example.multi.util

import android.content.Context
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import com.example.multi.Note

/**
 * Generates a .docx file for the given note and returns the created file.
 */
fun generateNoteDocx(context: Context, note: Note): File {
    val document = XWPFDocument()
    val title = document.createParagraph()
    val titleRun = title.createRun()
    titleRun.isBold = true
    titleRun.fontSize = 20
    titleRun.setText(note.header.ifBlank { "Note" })

    document.createParagraph().createRun().apply {
        setText(note.content)
    }

    val file = File.createTempFile("note_${'$'}{note.id}", ".docx", context.cacheDir)
    FileOutputStream(file).use { document.write(it) }
    document.close()
    return file
}
