package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.multi.EXTRA_NOTE_ID
import com.example.multi.EXTRA_NOTE_CONTENT
import com.example.multi.EXTRA_NOTE_CREATED
import com.example.multi.EXTRA_NOTE_HEADER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

class NotesActivity : SegmentActivity(
    "Notes",
    showBackButton = false,
    showCloseButton = false
) {
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
                Text(
                    "No notes yet",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    modifier = Modifier
                        .align(Alignment.Center)
                )
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
                            Text(
                                note.header.ifBlank { note.content.lines().take(3).joinToString("\n") },
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                                modifier = Modifier.padding(16.dp),
                                maxLines = 3
                            )
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
                text = { M3Text("Trashbin") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 80.dp)
            )
        }
    }
}
