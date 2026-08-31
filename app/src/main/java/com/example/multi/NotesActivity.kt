package com.example.multi

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.multi.di.ServiceLocator
import com.example.multi.ui.components.MonogramAvatar
import com.example.multi.ui.components.MultiCard
import com.example.multi.ui.components.Pill
import com.example.multi.ui.components.EmptyState
import com.example.multi.ui.notes.NotesViewModel
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.util.TextMetrics
import com.example.multi.util.shareNotesAsDocx
import com.example.multi.util.shareNotesAsPdf
import com.example.multi.util.shareNotesAsTxt
import com.example.multi.util.showModernToast
import com.example.multi.util.toDateString

class NotesActivity : SegmentActivity("Notes") {

    override val hasOverflowMenu: Boolean get() = true

    private val viewModel: NotesViewModel by viewModels {
        NotesViewModel.factory(ServiceLocator.provideNotesRepository(this))
    }

    private var importRequest: (() -> Unit)? = null

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val spacing = MultiTheme.spacing
        val notesAccent = MultiTheme.extended.notes

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val notes = uiState.notes
        var selectionMode by remember { mutableStateOf(false) }
        val selectedIds = remember { mutableStateListOf<Long>() }
        var shareMenuExpanded by remember { mutableStateOf(false) }

        val importLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                var name: String? = null
                context.contentResolver.query(
                    it, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
                )?.use { c ->
                    if (c.moveToFirst()) {
                        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) name = c.getString(idx)
                    }
                }
                viewModel.importNote(name ?: "Imported File", it.toString())
            }
        }
        importRequest = { importLauncher.launch(arrayOf("*/*")) }

        uiState.errorMessage?.let { message ->
            LaunchedEffect(message) {
                context.showModernToast(message)
                viewModel.consumeError()
            }
        }

        fun exitSelection() {
            selectedIds.clear()
            selectionMode = false
        }

        BackHandler(enabled = selectionMode) { exitSelection() }

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isEmpty) {
                EmptyState(
                    animation = R.raw.notebook,
                    title = "No notes yet",
                    subtitle = "Capture a thought, a list, or a whole draft. Everything you write here stays on your device.",
                    actionLabel = "New note",
                    onAction = {
                        context.startActivity(Intent(context, NoteEditorActivity::class.java))
                    },
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.gutter,
                        end = spacing.gutter,
                        top = spacing.sm,
                        bottom = spacing.fabClearance,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    items(notes, key = { it.id }) { note ->
                        val selected = note.id in selectedIds

                        fun toggle() {
                            if (selected) {
                                selectedIds.remove(note.id)
                                if (selectedIds.isEmpty()) selectionMode = false
                            } else {
                                selectedIds.add(note.id)
                            }
                        }

                        val isFileAttachment = note.attachmentUri != null &&
                            !note.attachmentUri!!.startsWith("event:")
                        val isEventAttachment = note.attachmentUri?.startsWith("event:") == true

                        MultiCard(
                            selected = selected,
                            contentPadding = PaddingValues(spacing.lg),
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        toggle()
                                    } else if (isFileAttachment) {
                                        val fileUri = Uri.parse(note.attachmentUri)
                                        context.contentResolver.takePersistableUriPermission(
                                            fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                        )
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW).apply {
                                                data = fileUri
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            },
                                        )
                                    } else {
                                        context.startActivity(
                                            Intent(context, NoteEditorActivity::class.java).apply {
                                                putExtra(EXTRA_NOTE_ID, note.id)
                                                putExtra(EXTRA_NOTE_HEADER, note.header)
                                                putExtra(EXTRA_NOTE_CONTENT, note.content)
                                                putExtra(EXTRA_NOTE_CREATED, note.created)
                                                putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                                                putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                                                putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                                            },
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!selectionMode) selectionMode = true
                                    toggle()
                                },
                            ),
                        ) {
                            NoteRow(
                                note = note,
                                selectionMode = selectionMode,
                                selected = selected,
                                accentContainer = notesAccent.container,
                                onAccentContainer = MaterialTheme.colorScheme.onSurface,
                                isFileAttachment = isFileAttachment,
                                isEventAttachment = isEventAttachment,
                                onOpenEvent = {
                                    val eventId = note.attachmentUri!!
                                        .removePrefix("event:").toLongOrNull()
                                    if (eventId != null) {
                                        context.startActivity(
                                            Intent(context, EventsActivity::class.java).apply {
                                                putExtra(EXTRA_EVENT_ID, eventId)
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectionMode,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = spacing.xl, start = spacing.lg, end = spacing.lg),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            viewModel.moveToTrash(selectedIds.toSet())
                            exitSelection()
                        },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { M3Text("Delete (${selectedIds.size})") },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )

                    Box {
                        ExtendedFloatingActionButton(
                            onClick = { shareMenuExpanded = true },
                            icon = { Icon(Icons.Default.Share, contentDescription = null) },
                            text = { M3Text("Share") },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        DropdownMenu(
                            expanded = shareMenuExpanded,
                            onDismissRequest = { shareMenuExpanded = false },
                        ) {
                            fun shareAs(block: (List<Note>) -> Unit) {
                                shareMenuExpanded = false
                                block(notes.filter { it.id in selectedIds })
                                exitSelection()
                            }
                            DropdownMenuItem(
                                text = { M3Text("Word (.docx)") },
                                onClick = { shareAs { shareNotesAsDocx(it, context) } },
                            )
                            DropdownMenuItem(
                                text = { M3Text("Plain text (.txt)") },
                                onClick = { shareAs { shareNotesAsTxt(it, context) } },
                            )
                            DropdownMenuItem(
                                text = { M3Text("PDF") },
                                onClick = { shareAs { shareNotesAsPdf(it, context) } },
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !selectionMode && !uiState.isEmpty,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = spacing.xl, end = spacing.xl),
            ) {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, NoteEditorActivity::class.java))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New note")
                }
            }
        }
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        val context = LocalContext.current
        DropdownMenuItem(
            text = { M3Text("Import a file") },
            onClick = {
                onDismiss()
                importRequest?.invoke()
            },
        )
        DropdownMenuItem(
            text = { M3Text("Trash") },
            onClick = {
                onDismiss()
                context.startActivity(Intent(context, TrashbinActivity::class.java))
            },
        )
    }
}

@Composable
private fun NoteRow(
    note: Note,
    selectionMode: Boolean,
    selected: Boolean,
    accentContainer: androidx.compose.ui.graphics.Color,
    onAccentContainer: androidx.compose.ui.graphics.Color,
    isFileAttachment: Boolean,
    isEventAttachment: Boolean,
    onOpenEvent: () -> Unit,
) {
    val spacing = MultiTheme.spacing
    val wordCount = remember(note.content) { TextMetrics.wordCount(note.content) }

    val title = note.header.trim().ifEmpty {
        note.content.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    }.ifEmpty { "Untitled note" }

    val snippet = buildList {
        if (note.header.isNotBlank()) {
            addAll(note.content.lines().map { it.trim() }.filter { it.isNotEmpty() })
        } else {
            addAll(
                note.content.lines().map { it.trim() }.filter { it.isNotEmpty() }.drop(1),
            )
        }
    }.take(2).joinToString(" ")

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (selectionMode) {
            Checkbox(checked = selected, onCheckedChange = null)
            Spacer(Modifier.width(spacing.sm))
        }

        if (isFileAttachment) {
            MonogramAvatar(
                label = "",
                container = accentContainer,
                onContainer = onAccentContainer,
            )
        } else {
            MonogramAvatar(
                label = title,
                container = accentContainer,
                onContainer = onAccentContainer,
            )
        }

        Spacer(Modifier.width(spacing.md))

        Column(Modifier.weight(1f)) {
            M3Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (snippet.isNotEmpty()) {
                Spacer(Modifier.height(spacing.xxs))
                M3Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Pill(text = note.created.toDateString(), icon = Icons.Default.CalendarToday)
                if (isFileAttachment) {
                    Pill(text = "File", icon = Icons.Default.AttachFile)
                } else if (isEventAttachment) {
                    Pill(
                        text = "Event",
                        icon = Icons.Default.Link,
                        accent = MultiTheme.extended.events.container,
                        onAccent = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable(onClick = onOpenEvent),
                    )
                } else if (wordCount > 0) {
                    Pill(text = pluralWords(wordCount))
                }
            }
        }
    }
}

private fun pluralWords(count: Int): String =
    if (count == 1) "1 word" else "$count words"
