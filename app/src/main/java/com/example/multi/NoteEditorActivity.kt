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
import com.example.multi.data.NoteEntity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val EXTRA_NOTE_ID = "extra_note_id"

class NoteEditorActivity : SegmentActivity("Note") {
    private var noteId: Long = -1L
    private var created: Long = System.currentTimeMillis()
    private var currentText: String = ""
    private var originalText: String = ""
    private var saved = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun SegmentContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val textState = remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                if (noteId >= 0) {
                    val dao = EventDatabase.getInstance(context).noteDao()
                    val entity = withContext(Dispatchers.IO) { dao.get(noteId) }
                    entity?.let {
                        textState.value = it.content
                        currentText = it.content
                        originalText = it.content
                        created = it.created
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
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
                            val dao = EventDatabase.getInstance(context).noteDao()
                            if (noteId >= 0) {
                                if (text.isNotEmpty() && text != originalText) {
                                    dao.update(NoteEntity(noteId, text, created))
                                }
                            } else if (text.isNotEmpty()) {
                                dao.insert(Note(content = text).toEntity())
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

                if (noteId >= 0) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            saved = true
                            scope.launch(Dispatchers.IO) {
                                EventDatabase.getInstance(context).noteDao().delete(noteId)
                            }
                            (context as? android.app.Activity)?.finish()
                        },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { Text("Delete") },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
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
                if (noteId >= 0) {
                    if (text != originalText) {
                        dao.update(NoteEntity(noteId, text, created))
                    }
                } else {
                    dao.insert(Note(content = text).toEntity())
                }
            }
        }
    }
}
