package com.example.multi

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.multi.util.NoteBlock
import com.example.multi.util.parseNoteBlocks

/** Display a note's content parsing [image] tokens. */
@Composable
fun NotePreview(text: String) {
    val blocks = text.parseNoteBlocks()
    Column {
        blocks.forEach { block ->
            when (block) {
                is NoteBlock.TextBlock -> {
                    if (block.text.isNotEmpty()) {
                        Text(block.text, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                is NoteBlock.ImageBlock -> {
                    AsyncImage(
                        model = Uri.parse(block.uri),
                        contentDescription = null,
                        modifier = Modifier.height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
