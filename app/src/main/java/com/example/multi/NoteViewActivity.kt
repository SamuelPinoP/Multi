package com.example.multi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

class NoteViewActivity : SegmentActivity("Note") {
    private var noteId: Long = 0L
    private var noteHeader: String = ""
    private var noteContent: String = ""
    private var noteCreated: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        noteHeader = intent.getStringExtra(EXTRA_NOTE_HEADER) ?: ""
        noteContent = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""
        noteCreated = intent.getLongExtra(EXTRA_NOTE_CREATED, 0L)
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun SegmentContent() {
        val blocks = parseNoteBlocks(noteContent)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (noteHeader.isNotBlank()) {
                Text(noteHeader, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
            blocks.forEach { block ->
                when {
                    block.text != null -> Text(block.text, style = MaterialTheme.typography.bodyLarge)
                    block.imageUri != null -> Image(
                        painter = rememberAsyncImagePainter(Uri.parse(block.imageUri)),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Created: ${'$'}{noteCreated.toDateString()}", style = MaterialTheme.typography.labelSmall)
        }
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        val context = LocalContext.current
        DropdownMenuItem(
            text = { Text("Edit") },
            onClick = {
                onDismiss()
                val intent = Intent(context, NoteEditorActivity::class.java).apply {
                    putExtra(EXTRA_NOTE_ID, noteId)
                    putExtra(EXTRA_NOTE_HEADER, noteHeader)
                    putExtra(EXTRA_NOTE_CONTENT, noteContent)
                    putExtra(EXTRA_NOTE_CREATED, noteCreated)
                }
                context.startActivity(intent)
            }
        )
    }
}

data class NoteBlock(val text: String? = null, val imageUri: String? = null)

fun parseNoteBlocks(text: String): List<NoteBlock> {
    val regex = Regex("!\\[[^\\]]*\\]\\(([^)]+)\\)")
    val blocks = mutableListOf<NoteBlock>()
    var index = 0
    for (match in regex.findAll(text)) {
        val start = match.range.first
        if (start > index) {
            blocks.add(NoteBlock(text = text.substring(index, start)))
        }
        blocks.add(NoteBlock(imageUri = match.groupValues[1]))
        index = match.range.last + 1
    }
    if (index < text.length) {
        blocks.add(NoteBlock(text = text.substring(index)))
    }
    return blocks
}
