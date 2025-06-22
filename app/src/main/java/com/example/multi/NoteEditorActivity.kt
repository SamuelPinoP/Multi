package com.example.multi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.TrashedNote
import androidx.lifecycle.lifecycleScope
import com.example.multi.util.toDateString
import com.example.multi.util.shareAsDocx
import com.example.multi.util.shareAsPdf
import com.example.multi.util.capitalizeAfterPeriod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

const val EXTRA_NOTE_ID = "extra_note_id"
const val EXTRA_NOTE_CONTENT = "extra_note_content"
const val EXTRA_NOTE_CREATED = "extra_note_created"
const val EXTRA_NOTE_HEADER = "extra_note_header"
const val EXTRA_NOTE_READ_ONLY = "extra_note_read_only"
const val EXTRA_NOTE_DELETED = "extra_note_deleted"

class NoteEditorActivity : SegmentActivity("Note") {
    private var noteId: Long = 0L
    private var noteCreated: Long = System.currentTimeMillis()
    private var noteDeleted: Long = 0L
    private var readOnly: Boolean = false
    private var currentHeader: String = ""
    private var currentText: String = ""
    private var saved = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        noteCreated = intent.getLongExtra(EXTRA_NOTE_CREATED, noteCreated)
        noteDeleted = intent.getLongExtra(EXTRA_NOTE_DELETED, 0L)
        readOnly = intent.getBooleanExtra(EXTRA_NOTE_READ_ONLY, false)
        currentHeader = intent.getStringExtra(EXTRA_NOTE_HEADER) ?: ""
        currentText = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun SegmentContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val headerState = remember { mutableStateOf(currentHeader) }
            val textState = remember { mutableStateOf(currentText) }
            var textSize by remember { mutableStateOf(20) }
            var showSizeDialog by remember { mutableStateOf(false) }
            var shareMenuExpanded by remember { mutableStateOf(false) }

            LaunchedEffect(headerState.value, textState.value) {
                if (!readOnly && !saved && (headerState.value.isNotBlank() || textState.value.isNotBlank())) {
                    delay(500)
                    val dao = EventDatabase.getInstance(context).noteDao()
                    withContext(Dispatchers.IO) {
                        if (noteId == 0L) {
                            noteId = dao.insert(
                                Note(
                                    header = headerState.value.trim(),
                                    content = textState.value.trim(),
                                    created = noteCreated
                                ).toEntity()
                            )
                        } else {
                            dao.update(
                                Note(
                                    id = noteId,
                                    header = headerState.value.trim(),
                                    content = textState.value.trim(),
                                    created = noteCreated
                                ).toEntity()
                            )
                        }
                    }
                    saved = true
                    currentHeader = headerState.value
                    currentText = textState.value
                }
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Created: ${noteCreated.toDateString()}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (readOnly && noteDeleted != 0L) {
                        val daysLeft = ((noteDeleted + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                        Text(
                            text = "Days remaining: $daysLeft",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Box {
                        if (headerState.value.isEmpty()) {
                            Text(
                                text = "Header",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = headerState.value,
                            onValueChange = {
                                if (it.lines().size <= 3) {
                                    val processed = it.capitalizeAfterPeriod()
                                    headerState.value = processed
                                    currentHeader = processed
                                    saved = false
                                }
                            },
                            enabled = !readOnly,
                            modifier = Modifier
                                .fillMaxWidth(),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = textSize.sp
                            ),
                            maxLines = 3
                        )
                    }

                    androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (textState.value.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = textState.value,
                            onValueChange = {
                                val processed = it.capitalizeAfterPeriod()
                                textState.value = processed
                                currentText = processed
                                saved = false
                            },
                            enabled = !readOnly,
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = textSize.sp
                            )
                        )
                    }
                }


                if (!readOnly && noteId != 0L) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            saved = true
                            scope.launch(Dispatchers.IO) {
                                val db = EventDatabase.getInstance(context)
                                val note = Note(id = noteId, header = currentHeader, content = currentText, created = noteCreated)
                                db.trashedNoteDao().insert(
                                    TrashedNote(header = note.header, content = note.content, created = note.created).toEntity()
                                )
                                db.noteDao().delete(note.toEntity())
                            }
                            (context as? android.app.Activity)?.finish()
                        },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { Text("Delete") },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 80.dp)
                    )
                }

                if (!readOnly) {
                    Box(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 80.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { shareMenuExpanded = true },
                            icon = { Icon(Icons.Default.Note, contentDescription = null) },
                            text = { Text("Share") },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        DropdownMenu(
                            expanded = shareMenuExpanded,
                            onDismissRequest = { shareMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Word") },
                                onClick = {
                                    shareMenuExpanded = false
                                    val note = Note(id = noteId, header = currentHeader, content = currentText, created = noteCreated)
                                    note.shareAsDocx(context)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("PDF") },
                                onClick = {
                                    shareMenuExpanded = false
                                    val note = Note(id = noteId, header = currentHeader, content = currentText, created = noteCreated)
                                    note.shareAsPdf(context)
                                }
                            )
                        }
                    }
                }

                if (!readOnly) {
                    ExtendedFloatingActionButton(
                        onClick = { showSizeDialog = true },
                    icon = { Icon(Icons.Default.FormatSize, contentDescription = null) },
                    text = { Text("Text Size") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                    )

                    if (showSizeDialog) {
                        AlertDialog(
                            onDismissRequest = { showSizeDialog = false },
                            confirmButton = {
                                TextButton(onClick = { showSizeDialog = false }) { Text("Close") }
                            },
                            title = { Text("Select Text Size") },
                            text = {
                                Column {
                                    listOf(16, 20, 24, 28, 32).forEach { size ->
                                        Button(
                                            onClick = {
                                                textSize = size
                                                showSizeDialog = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text("${size}sp")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val text = currentText.trim().capitalizeAfterPeriod()
        val header = currentHeader.trim().capitalizeAfterPeriod()
        if (!readOnly && !saved && (text.isNotEmpty() || header.isNotEmpty())) {
            saved = true
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(applicationContext).noteDao()
                if (noteId == 0L) {
                    dao.insert(Note(header = header, content = text, created = noteCreated).toEntity())
                } else {
                    dao.update(Note(id = noteId, header = header, content = text, created = noteCreated).toEntity())
                }
            }
        }
    }
}
