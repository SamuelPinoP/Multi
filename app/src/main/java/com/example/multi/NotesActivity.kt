package com.example.multi

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.util.shareNotesAsDocx
import com.example.multi.util.shareNotesAsPdf
import com.example.multi.util.shareNotesAsTxt
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.example.multi.data.toEntity

const val EXTRA_PICK_NOTE = "extra_pick_note"

class NotesActivity : SegmentActivity("Notes") {
    private val notes = mutableStateListOf<Note>()
    private var importRequest: (() -> Unit)? = null
    private var pickMode: Boolean = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        pickMode = intent.getBooleanExtra(EXTRA_PICK_NOTE, false)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val db = EventDatabase.getInstance(this@NotesActivity)
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { db.trashedNoteDao().deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { db.noteDao().getNotes() }
            notes.clear(); notes.addAll(stored.map { it.toModel() })
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { this@NotesActivity.notes }
        val pickMode = remember { this@NotesActivity.pickMode }
        var selectionMode by remember { mutableStateOf(false) }
        val selectedIds = remember { mutableStateListOf<Long>() }
        var shareMenuExpanded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                var name: String? = null
                context.contentResolver.query(it, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) name = c.getString(idx)
                    }
                }
                val newNote = Note(
                    header = name ?: "Imported File",
                    content = "",
                    created = System.currentTimeMillis(),
                    lastOpened = System.currentTimeMillis(),
                    attachmentUri = it.toString()
                )
                scope.launch {
                    val dao = EventDatabase.getInstance(context).noteDao()
                    val id = withContext(Dispatchers.IO) { dao.insert(newNote.toEntity()) }
                    notes.add(0, newNote.copy(id = id))
                }
            }
        }
        importRequest = { importLauncher.launch(arrayOf("*/*")) }

        if (!pickMode) {
            BackHandler(enabled = selectionMode) {
                selectedIds.clear()
                selectionMode = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (notes.isEmpty()) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.notebook)
                )
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(200.dp)
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
                                        if (pickMode) {
                                            val act = context as android.app.Activity
                                            act.setResult(android.app.Activity.RESULT_OK, Intent().putExtra(EXTRA_NOTE_ID, note.id))
                                            act.finish()
                                        } else if (selectionMode) {
                                            if (selected) {
                                                selectedIds.remove(note.id)
                                                if (selectedIds.isEmpty()) selectionMode = false
                                            } else {
                                                selectedIds.add(note.id)
                                            }
                                        } else {
                                            if (note.attachmentUri != null) {
                                                val uri = Uri.parse(note.attachmentUri)
                                                context.contentResolver.takePersistableUriPermission(
                                                    uri,
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                )
                                                val open = Intent(Intent.ACTION_VIEW).apply {
                                                    data = uri
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(open)
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
                                        }
                                    },
                                    onLongClick = {
                                        if (!pickMode) {
                                            if (!selectionMode) selectionMode = true
                                            if (selected) {
                                                selectedIds.remove(note.id)
                                                if (selectedIds.isEmpty()) selectionMode = false
                                            } else {
                                                selectedIds.add(note.id)
                                            }
                                        }
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!pickMode && selectionMode) {
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

            if (!pickMode && selectionMode) {
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
                                                created = note.created,
                                                attachmentUri = note.attachmentUri
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
            } else if (!pickMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            context.startActivity(Intent(context, NoteEditorActivity::class.java))
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        }
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        if (pickMode) return
        val context = LocalContext.current
        DropdownMenuItem(
            text = { M3Text("Import") },
            onClick = {
                onDismiss()
                importRequest?.invoke()
            }
        )
        DropdownMenuItem(
            text = { M3Text("Trash") },
            onClick = {
                onDismiss()
                context.startActivity(Intent(context, TrashbinActivity::class.java))
            }
        )
    }
}
