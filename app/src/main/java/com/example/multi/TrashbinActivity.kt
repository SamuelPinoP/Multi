package com.example.multi

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing deleted notes. */
class TrashbinActivity : SegmentActivity("Trash") {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { mutableStateListOf<TrashedNote>() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).trashedNoteDao()
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { dao.deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { dao.getNotes() }
            notes.clear(); notes.addAll(stored.map { it.toModel() })
        }

        TrashList(
            items = notes,
            deletedTime = { it.deleted },
            cardModifier = {
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, NoteEditorActivity::class.java)
                        intent.putExtra(EXTRA_NOTE_HEADER, it.header)
                        intent.putExtra(EXTRA_NOTE_CONTENT, it.content)
                        intent.putExtra(EXTRA_NOTE_CREATED, it.created)
                        intent.putExtra(EXTRA_NOTE_DELETED, it.deleted)
                        intent.putExtra(EXTRA_NOTE_COLOR, it.color)
                        intent.putExtra(EXTRA_NOTE_READ_ONLY, true)
                        context.startActivity(intent)
                    }
            },
            onRestore = { note ->
                scope.launch {
                    val db = EventDatabase.getInstance(context)
                    withContext(Dispatchers.IO) {
                        db.noteDao().insert(
                            Note(
                                header = note.header,
                                content = note.content,
                                created = note.created,
                                lastOpened = System.currentTimeMillis(),
                                attachmentUri = note.attachmentUri,
                                color = note.color
                            ).toEntity()
                        )
                        db.trashedNoteDao().delete(note.toEntity())
                    }
                    notes.remove(note)
                }
            },
            onDelete = { note ->
                scope.launch {
                    val dao = EventDatabase.getInstance(context).trashedNoteDao()
                    withContext(Dispatchers.IO) { dao.delete(note.toEntity()) }
                    notes.remove(note)
                }
            }
        ) { note ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val initial = (note.header.ifBlank { note.content }.trim().firstOrNull() ?: 'N').toString()
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val previewLines = mutableListOf<String>()
                    val headerLine = note.header.trim()
                    if (headerLine.isNotEmpty()) previewLines.add(headerLine)
                    previewLines.addAll(
                        note.content.lines()
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    )
                    val previewText = previewLines.take(2).joinToString("\n")
                    Text(
                        previewText,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Created: ${note.created.toDateString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
