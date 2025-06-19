package com.example.multi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

class NoteEditorActivity : SegmentActivity("Note") {
    private var noteId: Long = -1L
    private var created: Long = System.currentTimeMillis()
    private var currentText: String = ""
    private var saved = false
    private val textState = mutableStateOf("")

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra("note_id", -1L)
        super.onCreate(savedInstanceState)
        if (noteId > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(this@NoteEditorActivity).noteDao()
                dao.getNote(noteId)?.let { note ->
                    created = note.created
                    currentText = note.content
                    textState.value = note.content
                    saved = true
                }
            }
        }
    }

    @Composable
    override fun SegmentContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val textState = remember { this@NoteEditorActivity.textState }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                if (textState.value.isEmpty()) {
                    Text(
                        text = "Start writing...",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
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
                        fontSize = 20.sp
                    )
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        val text = textState.value.trim()
                        saved = true
                        currentText = text
                        scope.launch(Dispatchers.IO) {
                            if (text.isNotEmpty()) {
                                val dao = EventDatabase.getInstance(context).noteDao()
                                if (noteId > 0) {
                                    dao.update(Note(noteId, text, created).toEntity())
                                } else {
                                    noteId = dao.insert(Note(content = text).toEntity())
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

                if (noteId > 0) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                EventDatabase.getInstance(context).noteDao()
                                    .delete(Note(noteId, currentText, created).toEntity())
                            }
                            (context as? android.app.Activity)?.finish()
                        },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { Text("Delete") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
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
                if (noteId > 0) {
                    dao.update(Note(noteId, text, created).toEntity())
                } else {
                    noteId = dao.insert(Note(content = text).toEntity())
                }
            }
        }
    }
}
