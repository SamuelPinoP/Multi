package com.example.multi

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.util.capitalizeSentences
import com.example.multi.util.shareAsTxt
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.widget.EditText

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
    private val showColorDialogState = mutableStateOf(false)

    private var bodyEditor: EditText? = null

    private var textSize by textSizeState
    private var showSizeDialog by showSizeDialogState
    private var showColorDialog by showColorDialogState

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
            val headerBringIntoView = remember { BringIntoViewRequester() }
            val headerState = remember { mutableStateOf(currentHeader) }
            val textState = remember { mutableStateOf(currentText) }

            LaunchedEffect(headerState.value, textState.value) {
                val plainText = HtmlCompat.fromHtml(textState.value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                if (!readOnly && !saved && (headerState.value.isNotBlank() || plainText.isNotBlank())) {
                    val dao = EventDatabase.getInstance(context).noteDao()
                    noteScroll = bodyEditor?.scrollY ?: noteScroll
                    noteCursor = bodyEditor?.selectionStart ?: noteCursor
                    withContext(Dispatchers.IO) {
                        val formattedHeader = headerState.value.trim().capitalizeSentences()
                        val formattedContent = textState.value.trim()
                        if (noteId == 0L) {
                            noteId = dao.insert(
                                Note(
                                    header = formattedHeader,
                                    content = formattedContent,
                                    created = noteCreated,
                                    lastOpened = noteLastOpened,
                                    scroll = noteScroll,
                                    cursor = noteCursor,
                                    attachmentUri = null
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
                                    scroll = noteScroll,
                                    cursor = noteCursor,
                                    attachmentUri = null
                                ).toEntity()
                            )
                        }
                    }
                    saved = true
                    currentHeader = headerState.value
                    currentText = textState.value
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
                        .imePadding()
                ) {
                    Text(
                        text = "Created: ${noteCreated.toDateString()}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                    if (readOnly && noteDeleted != 0L) {
                        val daysLeft = ((noteDeleted + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                        Text(
                            text = "Days remaining: $daysLeft",
                            style = MaterialTheme.typography.labelSmall,
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
                                        scope.launch { headerBringIntoView.bringIntoView() }
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
                        if (HtmlCompat.fromHtml(textState.value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AndroidView(
                            factory = { ctx ->
                                EditText(ctx).apply {
                                    setText(HtmlCompat.fromHtml(currentText, HtmlCompat.FROM_HTML_MODE_LEGACY))
                                    setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                                    isEnabled = !readOnly
                                    setPadding(0, 0, 0, 0)
                                    addTextChangedListener(object : TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                        override fun afterTextChanged(s: Editable?) {
                                            val html = HtmlCompat.toHtml(s, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                                            currentText = html
                                            textState.value = html
                                            noteCursor = selectionStart
                                            saved = false
                                        }
                                    })
                                    post {
                                        setSelection(noteCursor.coerceIn(0, text.length))
                                    }
                                    post {
                                        scrollTo(0, noteScroll)
                                    }
                                }.also { bodyEditor = it }
                            },
                            update = { edit ->
                                edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
                                val current = HtmlCompat.fromHtml(textState.value, HtmlCompat.FROM_HTML_MODE_LEGACY)
                                if (HtmlCompat.toHtml(edit.text, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) != textState.value) {
                                    val sel = edit.selectionStart
                                    edit.setText(current)
                                    edit.setSelection(sel.coerceIn(0, edit.length()))
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (!readOnly) {
                    if (showSizeDialog) {
                        AlertDialog(
                            onDismissRequest = { showSizeDialog = false },
                            confirmButton = { TextButton(onClick = { showSizeDialog = false }) { Text("Close") } },
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
                    if (showColorDialog) {
                        AlertDialog(
                            onDismissRequest = { showColorDialog = false },
                            confirmButton = {},
                            title = { Text("Select Text Color") },
                            text = {
                                val colors = listOf(
                                    Color.Red, Color.Green, Color.Blue, Color.Yellow,
                                    Color.Magenta, Color.Cyan, Color.Black, Color.DarkGray
                                )
                                Column {
                                    colors.chunked(4).forEach { rowColors ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            rowColors.forEach { c ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .background(c, CircleShape)
                                                        .clickable {
                                                            bodyEditor?.let { edit ->
                                                                val start = edit.selectionStart
                                                                val end = edit.selectionEnd
                                                                if (start < end) {
                                                                    edit.text.setSpan(
                                                                        ForegroundColorSpan(c.toArgb()),
                                                                        start,
                                                                        end,
                                                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                                                    )
                                                                    currentText = HtmlCompat.toHtml(
                                                                        edit.text,
                                                                        HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL
                                                                    )
                                                                    textState.value = currentText
                                                                    saved = false
                                                                }
                                                            }
                                                            showColorDialog = false
                                                        }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
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
        /* No top bar actions - menu provided via overflow */
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        DropdownMenuItem(
            text = { Text("Share") },
            onClick = {
                onDismiss()
                val note = Note(
                    id = noteId,
                    header = currentHeader,
                    content = currentText,
                    created = noteCreated,
                    lastOpened = noteLastOpened,
                    attachmentUri = null
                )
                note.shareAsTxt(context)
            }
        )
        DropdownMenuItem(
            text = { Text("Text Size") },
            onClick = {
                onDismiss()
                showSizeDialog = true
            }
        )
        DropdownMenuItem(
            text = { Text("Text Color") },
            onClick = {
                onDismiss()
                showColorDialog = true
            }
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                onDismiss()
                if (noteId != 0L) {
                    saved = true
                    scope.launch(Dispatchers.IO) {
                        val db = EventDatabase.getInstance(context)
                        val note = Note(
                            id = noteId,
                            header = currentHeader,
                            content = currentText,
                            created = noteCreated,
                            lastOpened = noteLastOpened,
                            attachmentUri = null
                        )
                        db.trashedNoteDao().insert(
                            TrashedNote(
                                header = note.header,
                                content = note.content,
                                created = note.created,
                                attachmentUri = null
                            ).toEntity()
                        )
                        db.noteDao().delete(note.toEntity())
                    }
                    (context as? android.app.Activity)?.finish()
                }
            }
        )
    }

    override fun onStop() {
        super.onStop()
        bodyEditor?.let {
            currentText = HtmlCompat.toHtml(it.text, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
            noteCursor = it.selectionStart
            noteScroll = it.scrollY
        }
        val text = currentText.trim()
        val header = currentHeader.trim()
        if (!readOnly && !saved && (text.isNotEmpty() || header.isNotEmpty())) {
            saved = true
            lifecycleScope.launch(Dispatchers.IO) {
                val dao = EventDatabase.getInstance(applicationContext).noteDao()
                val formattedHeader = header.capitalizeSentences()
                val formattedText = text
                if (noteId == 0L) {
                    dao.insert(
                        Note(
                            header = formattedHeader,
                            content = formattedText,
                            created = noteCreated,
                            lastOpened = noteLastOpened,
                            scroll = noteScroll,
                            cursor = noteCursor,
                            attachmentUri = null
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
                            cursor = noteCursor,
                            attachmentUri = null
                        ).toEntity()
                    )
                }
            }
        }
    }
}
