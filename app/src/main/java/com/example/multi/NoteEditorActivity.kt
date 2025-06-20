package com.example.multi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
const val EXTRA_NOTE_CONTENT = "extra_note_content"
const val EXTRA_NOTE_CREATED = "extra_note_created"

class NoteEditorActivity : SegmentActivity("Note") {
    private var noteId: Long = 0L
    private var noteCreated: Long = System.currentTimeMillis()
    private var currentText: String = ""
    private var saved = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        noteCreated = intent.getLongExtra(EXTRA_NOTE_CREATED, noteCreated)
        currentText = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun SegmentContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val textState = remember { mutableStateOf(currentText) }
            val sizeOptions = listOf(16.sp, 20.sp, 24.sp, 28.sp)
            val fontSize = remember { mutableStateOf(20.sp) }
            val showMenu = remember { mutableStateOf(false) }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                if (textState.value.isEmpty()) {
                    Text(
                        text = "Start writing...",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.value),
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
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = fontSize.value
                    )
                )

                IconButton(
                    onClick = { showMenu.value = true },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Text Size")
                }
                DropdownMenu(expanded = showMenu.value, onDismissRequest = { showMenu.value = false }) {
                    sizeOptions.forEach { size ->
                        DropdownMenuItem(
                            text = { Text(size.value.toInt().toString()) },
                            onClick = {
                                fontSize.value = size
                                showMenu.value = false
                            }
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        val text = textState.value.trim()
                        saved = true
                        currentText = text
                        scope.launch(Dispatchers.IO) {
                            if (text.isNotEmpty()) {
                                val dao = EventDatabase.getInstance(context).noteDao()
                                if (noteId == 0L) {
                                    noteId = dao.insert(Note(content = text, created = noteCreated).toEntity())
                                } else {
                                    dao.update(Note(id = noteId, content = text, created = noteCreated).toEntity())
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
                                    .delete(Note(id = noteId, content = currentText, created = noteCreated).toEntity())
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
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val text = currentText.trim()
        if (!saved && text.isNotEmpty()) {
            saved = true
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(applicationContext).noteDao()
                if (noteId == 0L) {
                    dao.insert(Note(content = text, created = noteCreated).toEntity())
                } else {
                    dao.update(Note(id = noteId, content = text, created = noteCreated).toEntity())
                }
            }
        }
    }
}
