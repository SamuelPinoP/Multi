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
import androidx.compose.ui.graphics.lerp
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

        // Read theme color OUTSIDE remember; use it as a key.
        val primaryTint = MaterialTheme.colorScheme.primary
        val brandGradient = remember(primaryTint) {
            Brush.horizontalGradient(
                listOf(
                    lerp(primaryTint, Color.White, 0.12f),
                    lerp(primaryTint, Color.Black, 0.18f)
                )
            )
        }
        val brandBorder = Brush.horizontalGradient(
            listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.18f))
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Clear Trash button now matches TopAppBar color family
                GradientDangerButton(
                    text = "Clear trash",
                    enabled = notes.isNotEmpty(),
                    gradient = brandGradient,
                    borderBrush = brandBorder,
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

/* ---------- UI bits ---------- */

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
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .shadow(
                if (enabled) 12.dp else 0.dp,
                shape = shape,
                clip = false,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
            .background(
                if (enabled) gradient
                else Brush.linearGradient(listOf(Color(0xFF616161), Color(0xFF4A4A4A))),
                shape
            )
            .border(
                1.dp,
                if (enabled) borderBrush
                else Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))),
                shape
            )
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
        val p = MaterialTheme.colorScheme.primary
        val headerGrad = Brush.horizontalGradient(
            listOf(lerp(p, Color.White, 0.12f), lerp(p, Color.Black, 0.18f))
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
                            containerColor = MaterialTheme.colorScheme.error, // destructive stays red
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
