package com.example.multi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing notes in the trash bin. */
class TrashbinActivity : SegmentActivity("Trash Bin") {
    private val deletedNotes = mutableStateListOf<Note>()

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val dao = EventDatabase.getInstance(this@TrashbinActivity).noteDao()
            val monthAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            val stored = withContext(Dispatchers.IO) {
                dao.purgeDeleted(monthAgo)
                dao.getDeletedNotes()
            }
            deletedNotes.clear(); deletedNotes.addAll(stored.map { it.toModel() })
        }
    }

    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { deletedNotes }

        Box(modifier = Modifier.fillMaxSize()) {
            if (notes.isEmpty()) {
                Text("Trashbin is empty", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes) { note ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                note.header.ifBlank { note.content.lines().firstOrNull() ?: "" },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(
                                onClick = {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        EventDatabase.getInstance(context).noteDao()
                                            .update(note.copy(deleted = null).toEntity())
                                    }
                                    deletedNotes.remove(note)
                                },
                                modifier = Modifier.padding(16.dp)
                            ) { Text("Restore") }
                        }
                    }
                }
            }
        }
    }
}
