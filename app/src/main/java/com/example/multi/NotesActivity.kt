package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

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

    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { this@NotesActivity.notes }

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
                        ElevatedCard(
                            elevation = CardDefaults.elevatedCardElevation(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(context, NoteEditorActivity::class.java)
                                    intent.putExtra(EXTRA_NOTE_ID, note.id)
                                    intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                                    intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                                    intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                                    context.startActivity(intent)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
