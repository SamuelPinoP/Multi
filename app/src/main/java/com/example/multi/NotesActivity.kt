package com.example.multi

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.util.shareNotesAsDocx
import com.example.multi.util.shareNotesAsPdf
import com.example.multi.util.shareNotesAsTxt
import com.example.multi.util.toDateString

class NotesActivity : SegmentActivity("Notes") {
    private val viewModel: NotesViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.loadNotes()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes by viewModel.notes.collectAsState()
        var selectionMode by remember { mutableStateOf(false) }
        val selectedIds = remember { mutableStateListOf<Long>() }
        var shareMenuExpanded by remember { mutableStateOf(false) }

        BackHandler(enabled = selectionMode) {
            selectedIds.clear()
            selectionMode = false
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Note,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    M3Text(
                        "No notes yet",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes) { note ->
                        val selected = note.id in selectedIds
                        ElevatedCard(
                            elevation = CardDefaults.elevatedCardElevation(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (selectionMode) {
                                            if (selected) {
                                                selectedIds.remove(note.id)
                                                if (selectedIds.isEmpty()) selectionMode = false
                                            } else {
                                                selectedIds.add(note.id)
                                            }
                                        } else {
                                            val intent = Intent(context, NoteEditorActivity::class.java)
                                            intent.putExtra(EXTRA_NOTE_ID, note.id)
                                            intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                                            intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                                            intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                                            intent.putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                                            intent.putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                                            context.startActivity(intent)
                                        }
                                    },
                                    onLongClick = {
                                        if (!selectionMode) selectionMode = true
                                        if (selected) {
                                            selectedIds.remove(note.id)
                                            if (selectedIds.isEmpty()) selectionMode = false
                                        } else {
                                            selectedIds.add(note.id)
                                        }
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectionMode) {
                                    Checkbox(
                                        checked = selected,
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val initial = (note.header.ifBlank { note.content }.trim().firstOrNull() ?: 'N').toString()
                                        M3Text(
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
                                    M3Text(
                                        text = previewText,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    M3Text(
                                        text = note.created.toDateString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectionMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val targets = notes.filter { it.id in selectedIds }
                            viewModel.deleteNotes(targets)
                            selectedIds.clear()
                            selectionMode = false
                        },
                        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        text = { M3Text("Delete") },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Box {
                        ExtendedFloatingActionButton(
                            onClick = { shareMenuExpanded = true },
                            icon = { Icon(Icons.Default.Share, contentDescription = null) },
                            text = { M3Text("Share") },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        DropdownMenu(
                            expanded = shareMenuExpanded,
                            onDismissRequest = { shareMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { M3Text("Word") },
                                onClick = {
                                    shareMenuExpanded = false
                                    val targets = notes.filter { it.id in selectedIds }
                                    shareNotesAsDocx(targets, context)
                                    selectedIds.clear()
                                    selectionMode = false
                                }
                            )
                            DropdownMenuItem(
                                text = { M3Text("Text File") },
                                onClick = {
                                    shareMenuExpanded = false
                                    val targets = notes.filter { it.id in selectedIds }
                                    shareNotesAsTxt(targets, context)
                                    selectedIds.clear()
                                    selectionMode = false
                                }
                            )
                            DropdownMenuItem(
                                text = { M3Text("PDF") },
                                onClick = {
                                    shareMenuExpanded = false
                                    val targets = notes.filter { it.id in selectedIds }
                                    shareNotesAsPdf(targets, context)
                                    selectedIds.clear()
                                    selectionMode = false
                                }
                            )
                        }
                    }
                }
            } else {
                var fabExpanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (fabExpanded) {
                        FloatingActionButton(
                            onClick = {
                                context.startActivity(Intent(context, NoteEditorActivity::class.java))
                                fabExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) { Icon(Icons.Default.Add, contentDescription = "New Note") }

                        FloatingActionButton(
                            onClick = {
                                context.startActivity(Intent(context, TrashbinActivity::class.java))
                                fabExpanded = false
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) { Icon(Icons.Default.Delete, contentDescription = "Trash") }
                    }

                    FloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Menu"
                        )
                    }
                }
            }
        }
    }
}
