package com.example.multi

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    private val notes = mutableStateListOf<TrashedNote>()
    private val showConfirm = mutableStateOf(false)

    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).trashedNoteDao()
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { dao.deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { dao.getNotes() }
            notes.clear(); notes.addAll(stored.map { it.toModel() })
        }

        Box(modifier = Modifier.fillMaxSize()) {
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
                                    attachmentUri = note.attachmentUri
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

            if (showConfirm.value) {
                AlertDialog(
                    onDismissRequest = { showConfirm.value = false },
                    title = { Text("Clear trash?") },
                    text = { Text("This will permanently delete ${notes.size} notes.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirm.value = false
                            scope.launch {
                                val dao = EventDatabase.getInstance(context).trashedNoteDao()
                                withContext(Dispatchers.IO) { dao.deleteAll() }
                                notes.clear()
                                snackbarHostState.showSnackbar("Trash cleared.")
                            }
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm.value = false }) { Text("Cancel") }
                    }
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )
        }
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        DropdownMenuItem(
            text = { Text("Clear trash", color = MaterialTheme.colorScheme.error) },
            enabled = notes.isNotEmpty(),
            onClick = {
                showConfirm.value = true
                onDismiss()
            }
        )
    }
}

