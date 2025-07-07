package com.example.multi

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.stringResource

/** Activity showing deleted notes. */
class TrashbinActivity : SegmentActivity(R.string.label_trash) {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { mutableStateListOf<TrashedNote>() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).trashedNoteDao()
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { dao.deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { dao.getNotes() }
            notes.clear(); notes.addAll(stored.map { it.toModel() })
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (notes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.trash_empty),
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
                        val daysLeft = ((note.deleted + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
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
                                    intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                                    intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                                    intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                                    intent.putExtra(EXTRA_NOTE_DELETED, note.deleted)
                                    intent.putExtra(EXTRA_NOTE_READ_ONLY, true)
                                    context.startActivity(intent)
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            val initial = (note.header.ifBlank { note.content }.trim().firstOrNull() ?: 'N').toString()
                                            Text(
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
                                        Text(
                                            previewText,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 2
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            stringResource(R.string.created, note.created.toDateString()),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            stringResource(R.string.days_remaining, daysLeft),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        scope.launch {
                                            val db = EventDatabase.getInstance(context)
                                            withContext(Dispatchers.IO) {
                                                db.noteDao().insert(
                                                    Note(
                                                        header = note.header,
                                                        content = note.content,
                                                        created = note.created,
                                                        lastOpened = System.currentTimeMillis()
                                                    ).toEntity()
                                                )
                                                db.trashedNoteDao().delete(note.toEntity())
                                            }
                                            notes.remove(note)
                                        }
                                    }) { Text(stringResource(R.string.restore)) }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        scope.launch {
                                            val dao = EventDatabase.getInstance(context).trashedNoteDao()
                                            withContext(Dispatchers.IO) { dao.delete(note.toEntity()) }
                                            notes.remove(note)
                                        }
                                    }) { Text(stringResource(R.string.delete)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
