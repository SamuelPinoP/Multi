package com.example.multi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.util.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing deleted notes. */
class TrashbinActivity : SegmentActivity("Trash") {

    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val notes = remember { mutableStateListOf<TrashedNote>() }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        var showConfirm by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).trashedNoteDao()
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { dao.deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { dao.getNotes() }
            notes.clear(); notes.addAll(stored.map { it.toModel() })
        }

        // —— Pretty colors for destructive actions (kept here for theming flexibility)
        val dangerGradient = Brush.horizontalGradient(
            listOf(
                Color(0xFFFF3D47),  // neon red
                Color(0xFFCC1E5A)   // magenta-crimson
            )
        )
        val dangerBorder = Brush.horizontalGradient(
            listOf(Color(0x66FFFFFF), Color(0x33FFFFFF))
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Gorgeous "Clear trash" button (pill, glow, icon)
                GradientDangerButton(
                    text = "Clear trash",
                    enabled = notes.isNotEmpty(),
                    gradient = dangerGradient,
                    borderBrush = dangerBorder,
                    onClick = { showConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                TrashList(
                    items = notes,
                    deletedTime = { it.deleted },
                    cardModifier = {
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(context, NoteEditorActivity::class.java)
                                intent.putExtra(EXTRA_NOTE_HEADER, it.header)
                                intent.putExtra(EXTRA_NOTE_CONTENT, it.content)
                                intent.putExtra(EXTRA_NOTE_CREATED, it.created)
                                intent.putExtra(EXTRA_NOTE_DELETED, it.deleted)
                                intent.putExtra(EXTRA_NOTE_READ_ONLY, true)
                                context.startActivity(intent)
                            }
                    },
                    onRestore = { note ->
                        scope.launch {
                            val db = EventDatabase.getInstance(context)
                            withContext(Dispatchers.IO) {
                                db.noteDao().insert(
                                    Note(
                                        header = note.header,
                                        content = note.content,
                                        created = note.created,
                                        lastOpened = System.currentTimeMillis(),
                                        attachmentUri = note.attachmentUri
                                    ).toEntity()
                                )
                                db.trashedNoteDao().delete(note.toEntity())
                            }
                            notes.remove(note)
                            snackbarHostState.showSnackbar("Note restored.")
                        }
                    },
                    onDelete = { note ->
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).trashedNoteDao()
                            withContext(Dispatchers.IO) { dao.delete(note.toEntity()) }
                            notes.remove(note)
                            snackbarHostState.showSnackbar("Deleted forever.")
                        }
                    }
                ) { note ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initial = (note.header.ifBlank { note.content }
                                    .trim().firstOrNull() ?: 'N').toString()
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
                                "Created: ${note.created.toDateString()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            )

            // Custom glassy confirmation dialog
            PrettyConfirmDialog(
                visible = showConfirm,
                count = notes.size,
                onCancel = { showConfirm = false },
                onConfirm = {
                    showConfirm = false
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).trashedNoteDao()
                        withContext(Dispatchers.IO) { dao.deleteAll() }
                        notes.clear()
                        snackbarHostState.showSnackbar("Trash cleared.")
                    }
                }
            )
        }
    }
}

/* ---------- Fancy UI bits ---------- */

@Composable
private fun GradientDangerButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    gradient: Brush,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    // Use a transparent Button with our own gradient background + glow
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .shadow(if (enabled) 12.dp else 0.dp, shape, ambientColor = Color.Black.copy(0.35f))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFF616161), Color(0xFF4A4A4A))), shape)
            .border(1.dp, if (enabled) borderBrush else Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))), shape)
            .clip(shape)
    ) {
        Icon(
            imageVector = Icons.Filled.DeleteForever,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun PrettyConfirmDialog(
    visible: Boolean,
    count: Int,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    Dialog(onDismissRequest = onCancel) {
        val shape = RoundedCornerShape(24.dp)
        val border = Brush.linearGradient(
            listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
        )
        val headerGrad = Brush.horizontalGradient(
            listOf(MaterialTheme.colorScheme.error, Color(0xFFCC1E5A))
        )

        Surface(
            shape = shape,
            tonalElevation = 4.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, shape)
        ) {
            Column(Modifier.padding(20.dp)) {
                // Header badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(headerGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            tint = Color.White,
                            contentDescription = null
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Clear trash?",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            "This will permanently delete $count ${if (count == 1) "note" else "notes"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
