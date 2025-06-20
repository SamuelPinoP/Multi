package com.example.multi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val EXTRA_NOTE_ID = "extra_note_id"
const val EXTRA_NOTE_HEADER = "extra_note_header"
const val EXTRA_NOTE_CONTENT = "extra_note_content"
const val EXTRA_NOTE_CREATED = "extra_note_created"

class NoteEditorActivity : SegmentActivity("Note") {
    private var noteId: Long = 0L
    private var noteCreated: Long = System.currentTimeMillis()
    private var currentHeader: String = ""
    private var currentText: String = ""
    private var saved = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        noteCreated = intent.getLongExtra(EXTRA_NOTE_CREATED, noteCreated)
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

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (headerState.value.isEmpty()) {
                        Text(
                            text = "Header",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = (textSize + 4).sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    BasicTextField(
                        value = headerState.value,
                        onValueChange = {
                            headerState.value = it
                            currentHeader = it
                            saved = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = (textSize + 4).sp
                        ),
                        maxLines = 3
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

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
                            textState.value = it
                            currentText = it
                            saved = false
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f, fill = true),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = textSize.sp
                        )
                    )
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        val header = headerState.value.trim()
                        val text = textState.value.trim()
                        saved = true
                        currentHeader = header
                        currentText = text
                        scope.launch(Dispatchers.IO) {
                            if (header.isNotEmpty() || text.isNotEmpty()) {
                                val dao = EventDatabase.getInstance(context).noteDao()
                                if (noteId == 0L) {
                                    noteId = dao.insert(
                                        Note(header = header, content = text, created = noteCreated).toEntity()
                                    )
                                } else {
                                    dao.update(
                                        Note(id = noteId, header = header, content = text, created = noteCreated).toEntity()
                                    )
                                }
                            }
                        }
                        (context as? android.app.Activity)?.finish()
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                    text = { Text("Save") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                )

                if (noteId != 0L) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            saved = true
                            scope.launch(Dispatchers.IO) {
                                EventDatabase.getInstance(context).noteDao()
                                    .delete(
                                        Note(
                                            id = noteId,
                                            header = currentHeader,
                                            content = currentText,
                                            created = noteCreated
                                        ).toEntity()
                                    )
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

    override fun onStop() {
        super.onStop()
        val header = currentHeader.trim()
        val text = currentText.trim()
        if (!saved && (header.isNotEmpty() || text.isNotEmpty())) {
            saved = true
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(applicationContext).noteDao()
                if (noteId == 0L) {
                    dao.insert(
                        Note(header = header, content = text, created = noteCreated).toEntity()
                    )
                } else {
                    dao.update(
                        Note(id = noteId, header = header, content = text, created = noteCreated).toEntity()
                    )
                }
            }
        }
    }
}
