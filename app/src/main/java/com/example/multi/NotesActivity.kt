package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesActivity : SegmentActivity(
    "Notes",
    showBackButton = false,
    showCloseButton = false
) {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { mutableStateListOf<Note>() }
        val scope = rememberCoroutineScope()

        suspend fun loadNotes() {
            val dao = EventDatabase.getInstance(context).noteDao()
            val stored = withContext(Dispatchers.IO) { dao.getNotes() }
            notes.clear()
            notes.addAll(stored.map { it.toModel() })
        }

        LaunchedEffect(Unit) { loadNotes() }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    scope.launch { loadNotes() }
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
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
        }
    }
}
