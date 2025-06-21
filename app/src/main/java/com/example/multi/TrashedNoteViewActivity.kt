package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.Note
import com.example.multi.TrashedNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrashedNoteViewActivity : SegmentActivity("Trashed Note") {
    private var noteId: Long = 0L
    private var noteHeader: String = ""
    private var noteContent: String = ""
    private var noteCreated: Long = 0L
    private var noteDeleted: Long = 0L

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)
        noteHeader = intent.getStringExtra(EXTRA_NOTE_HEADER) ?: ""
        noteContent = intent.getStringExtra(EXTRA_NOTE_CONTENT) ?: ""
        noteCreated = intent.getLongExtra(EXTRA_NOTE_CREATED, 0L)
        noteDeleted = intent.getLongExtra(EXTRA_NOTE_DELETED, 0L)
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val remaining = daysUntil(noteDeleted, 30)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = noteHeader.ifBlank { noteContent.lines().firstOrNull() ?: "" },
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(noteContent, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Created: ${formatDate(noteCreated)}", style = MaterialTheme.typography.labelSmall)
            Text("$remaining days remaining", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    scope.launch {
                        val db = EventDatabase.getInstance(context)
                        withContext(Dispatchers.IO) {
                            db.noteDao().insert(
                                Note(header = noteHeader, content = noteContent, created = noteCreated).toEntity()
                            )
                            db.trashedNoteDao().delete(TrashedNote(noteId, noteHeader, noteContent, noteCreated, noteDeleted).toEntity())
                        }
                    }
                    context.startActivity(Intent(context, NotesActivity::class.java))
                    finish()
                }) {
                    Text("Restore")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).trashedNoteDao()
                        withContext(Dispatchers.IO) { dao.delete(TrashedNote(noteId, noteHeader, noteContent, noteCreated, noteDeleted).toEntity()) }
                    }
                    finish()
                }) {
                    Text("Delete")
                }
            }
        }
    }
}
