package com.example.multi

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.focus.onFocusEvent
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import androidx.lifecycle.lifecycleScope
import com.example.multi.util.capitalizeSentences
import com.example.multi.util.toDateString
import com.example.multi.util.shareAsDocx
import com.example.multi.util.shareAsPdf
import com.example.multi.util.shareAsTxt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
const val EXTRA_NOTE_SCROLL = "extra_note_scroll"
const val EXTRA_NOTE_CURSOR = "extra_note_cursor"

class NoteEditorActivity : SegmentActivity("Note") {
    private var noteId: Long = 0L
    private var noteCreated: Long = System.currentTimeMillis()
    private var noteLastOpened: Long = System.currentTimeMillis()
    private var noteDeleted: Long = 0L
    private var noteScroll: Int = 0
    private var noteCursor: Int = 0
    private var readOnly: Boolean = false
    private var currentHeader: String = ""
    private var currentText: String = ""
    private var saved = false

    private val textSizeState = mutableIntStateOf(20)
    private val showSizeDialogState = mutableStateOf(false)
    private val shareMenuExpandedState = mutableStateOf(false)
    private val overflowMenuExpandedState = mutableStateOf(false)

    private var textSize by textSizeState
    private var showSizeDialog by showSizeDialogState
    private var shareMenuExpanded by shareMenuExpandedState
    private var overflowMenuExpanded by overflowMenuExpandedState

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        noteCreated = intent.getLongExtra(EXTRA_NOTE_CREATED, noteCreated)
        noteDeleted = intent.getLongExtra(EXTRA_NOTE_DELETED, 0L)
        readOnly = intent.getBooleanExtra(EXTRA_NOTE_READ_ONLY, false)
        currentHeader = intent.getStringExtra(EXTRA_NOTE_HEADER) ?: ""
        currentText = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""
        noteScroll = intent.getIntExtra(EXTRA_NOTE_SCROLL, 0)
        noteCursor = intent.getIntExtra(EXTRA_NOTE_CURSOR, 0)
        noteLastOpened = System.currentTimeMillis()
        if (noteId != 0L && !readOnly) {
            lifecycleScope.launch {
                val dao = EventDatabase.getInstance(this@NoteEditorActivity).noteDao()
                withContext(Dispatchers.IO) { dao.touch(noteId, noteLastOpened) }
            }
        }
        super.onCreate(savedInstanceState)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun SegmentContent() {
        Surface(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val scrollState = rememberScrollState(initial = noteScroll)
            val headerBringIntoView = remember { BringIntoViewRequester() }
            val textBringIntoView = remember { BringIntoViewRequester() }
            val headerState = remember { mutableStateOf(currentHeader) }
            val textState = remember { mutableStateOf(TextFieldValue(currentText, TextRange(noteCursor))) }
            var textSize by textSizeState
            var showSizeDialog by showSizeDialogState
            var shareMenuExpanded by shareMenuExpandedState
            var overflowMenuExpanded by overflowMenuExpandedState
            val density = LocalDensity.current

            LaunchedEffect(scrollState.value) { noteScroll = scrollState.value }
            LaunchedEffect(textState.value.selection) { noteCursor = textState.value.selection.start }

            LaunchedEffect(headerState.value, textState.value) {
                if (!readOnly && !saved && (headerState.value.isNotBlank() || textState.value.text.isNotBlank())) {
                    delay(500)
                    val dao = EventDatabase.getInstance(context).noteDao()
                    withContext(Dispatchers.IO) {
                        val formattedHeader = headerState.value.trim().capitalizeSentences()
                        val formattedContent = textState.value.text.trim().capitalizeSentences()
                        if (noteId == 0L) {
                            noteId = dao.insert(
                                Note(
                                    header = formattedHeader,
                                    content = formattedContent,
                                    created = noteCreated,
                                    lastOpened = noteLastOpened,
                                    scroll = scrollState.value,
                                    cursor = textState.value.selection.start
                                ).toEntity()
                            )
                        } else {
                            dao.update(
                                Note(
                                    id = noteId,
                                    header = formattedHeader,
                                    content = formattedContent,
                                    created = noteCreated,
                                    lastOpened = noteLastOpened,
                                    scroll = scrollState.value,
                                    cursor = textState.value.selection.start
                                ).toEntity()
                            )
                        }
                    }
                    saved = true
                    currentHeader = headerState.value
                    currentText = textState.value.text
                    noteScroll = scrollState.value
                    noteCursor = textState.value.selection.start
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .imePadding()
                ) {
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
                                    val formatted = it.capitalizeSentences()
                                    headerState.value = formatted
                                    currentHeader = formatted
                                    saved = false
                                }
                            },
                            enabled = !readOnly,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(headerBringIntoView)
                                .onFocusEvent {
                                    if (it.isFocused) {
                                        scope.launch {
                                            headerBringIntoView.bringIntoView()
                                        }
                                    }
                                },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = textSize.sp
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            maxLines = 3
                        )
                    }

                    androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (textState.value.text.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = textState.value,
                            onValueChange = {
                                val formatted = it.text.capitalizeSentences()
                                textState.value = it.copy(text = formatted)
                                currentText = formatted
                                noteCursor = it.selection.start
                                saved = false
                            },
                            enabled = !readOnly,
                            modifier = Modifier
                                .fillMaxSize()
                                .bringIntoViewRequester(textBringIntoView)
                                .onFocusEvent {
                                    if (it.isFocused) {
                                        scope.launch {
                                            textBringIntoView.bringIntoView()
                                        }
                                    }
                                },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = textSize.sp
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                capitalization = KeyboardCapitalization.Sentences
                            )
                        )
                    }
                }


                if (!readOnly) {
                    // Top bar actions handle share and delete. Only dialog is shown here.
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

    @Composable
    override fun SegmentActions() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        Box {
            IconButton(onClick = { shareMenuExpanded = true }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            DropdownMenu(
                expanded = shareMenuExpanded,
                onDismissRequest = { shareMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Word") },
                    onClick = {
                        shareMenuExpanded = false
                        val note = Note(
                            id = noteId,
                            header = currentHeader,
                            content = currentText,
                            created = noteCreated,
                            lastOpened = noteLastOpened
                        )
                        note.shareAsDocx(context)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Text File") },
                    onClick = {
                        shareMenuExpanded = false
                        val note = Note(
                            id = noteId,
                            header = currentHeader,
                            content = currentText,
                            created = noteCreated,
                            lastOpened = noteLastOpened
                        )
                        note.shareAsTxt(context)
                    }
                )
                DropdownMenuItem(
                    text = { Text("PDF") },
                    onClick = {
                        shareMenuExpanded = false
                        val note = Note(
                            id = noteId,
                            header = currentHeader,
                            content = currentText,
                            created = noteCreated,
                            lastOpened = noteLastOpened
                        )
                        note.shareAsPdf(context)
                    }
                )
            }
        }

        Box {
            IconButton(onClick = { overflowMenuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = overflowMenuExpanded,
                onDismissRequest = { overflowMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Text Size") },
                    onClick = {
                        overflowMenuExpanded = false
                        showSizeDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        overflowMenuExpanded = false
                        if (noteId != 0L) {
                            saved = true
                            scope.launch(Dispatchers.IO) {
                                val db = EventDatabase.getInstance(context)
                                val note = Note(
                                    id = noteId,
                                    header = currentHeader,
                                    content = currentText,
                                    created = noteCreated,
                                    lastOpened = noteLastOpened
                                )
                                db.trashedNoteDao().insert(
                                    TrashedNote(
                                        header = note.header,
                                        content = note.content,
                                        created = note.created
                                    ).toEntity()
                                )
                                db.noteDao().delete(note.toEntity())
                            }
                            (context as? android.app.Activity)?.finish()
                        }
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        val text = currentText.trim()
        val header = currentHeader.trim()
        if (!readOnly && !saved && (text.isNotEmpty() || header.isNotEmpty())) {
            saved = true
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(applicationContext).noteDao()
                val formattedHeader = header.capitalizeSentences()
                val formattedText = text.capitalizeSentences()
                if (noteId == 0L) {
                    dao.insert(
                        Note(
                            header = formattedHeader,
                            content = formattedText,
                            created = noteCreated,
                            lastOpened = noteLastOpened,
                            scroll = noteScroll,
                            cursor = noteCursor
                        ).toEntity()
                    )
                } else {
                    dao.update(
                        Note(
                            id = noteId,
                            header = formattedHeader,
                            content = formattedText,
                            created = noteCreated,
                            lastOpened = noteLastOpened,
                            scroll = noteScroll,
                            cursor = noteCursor
                        ).toEntity()
                    )
                }
            }
        }
    }
}
