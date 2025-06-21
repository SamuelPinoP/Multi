package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
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
import com.example.multi.TrashedNote
import com.example.multi.Note
import com.example.multi.DateUtils
import com.example.multi.EXTRA_NOTE_HEADER
import com.example.multi.EXTRA_NOTE_CONTENT
import com.example.multi.EXTRA_NOTE_CREATED
import com.example.multi.EXTRA_READ_ONLY
import com.example.multi.NoteEditorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing deleted notes. */
class TrashbinActivity : SegmentActivity("Trash Bin") {
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
                Text(
                    "Trashbin is empty",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes) { note ->
                        val expiresAt = note.deleted + 30L * 24 * 60 * 60 * 1000
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(context, NoteEditorActivity::class.java)
                                    intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                                    intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                                    intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                                    intent.putExtra(EXTRA_READ_ONLY, true)
                                    context.startActivity(intent)
                                },
                            elevation = CardDefaults.elevatedCardElevation()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    note.header.ifBlank { note.content.lines().take(3).joinToString("\n") },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Days remaining: ${DateUtils.daysRemaining(expiresAt)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
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
                                                    Note(header = note.header, content = note.content, created = note.created).toEntity()
                                                )
                                                db.trashedNoteDao().delete(note.toEntity())
                                            }
                                            notes.remove(note)
                                        }
                                    }) { Text("Restore") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        scope.launch {
                                            val dao = EventDatabase.getInstance(context).trashedNoteDao()
                                            withContext(Dispatchers.IO) { dao.delete(note.toEntity()) }
                                            notes.remove(note)
                                        }
                                    }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
