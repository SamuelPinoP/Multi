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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Note
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.util.shareNotesAsDocx
import com.example.multi.util.shareNotesAsPdf
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.toEntity

class NotesActivity : SegmentActivity("Notes") {
    private val notes = mutableStateListOf<Note>()

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val dao = EventDatabase.getInstance(this@NotesActivity).noteDao()
            val stored = withContext(Dispatchers.IO) { dao.getNotes() }
            notes.clear(); notes.addAll(stored.map { it.toModel() })
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { this@NotesActivity.notes }
        var selectionMode by remember { mutableStateOf(false) }
        val selectedIds = remember { mutableStateListOf<Long>() }
        var shareMenuExpanded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

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
                            scope.launch {
                                val db = EventDatabase.getInstance(context)
                                withContext(Dispatchers.IO) {
                                    val noteDao = db.noteDao()
                                    val trashDao = db.trashedNoteDao()
                                    targets.forEach { note ->
                                        trashDao.insert(
                                            TrashedNote(
                                                header = note.header,
                                                content = note.content,
                                                created = note.created
                                            ).toEntity()
                                        )
                                        noteDao.delete(note.toEntity())
                                    }
                                }
                                notes.removeAll { it.id in selectedIds }
                                selectedIds.clear()
                                selectionMode = false
                            }
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
                ExtendedFloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, NoteEditorActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { M3Text("New Note") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        context.startActivity(Intent(context, TrashbinActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    text = { M3Text("Trash") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                )
            }
        }
    }
}
