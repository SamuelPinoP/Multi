package com.example.multi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

const val EXTRA_DATE = "extra_date"
const val EXTRA_EVENT_ID = "extra_event_id"

/** Activity displaying the list of user events. */
class EventsActivity : SegmentActivity("Events") {
    private val events = mutableStateListOf<Event>()
    private val eventNotes = mutableStateMapOf<Long, Note>()
    private val showAttachDialogState = mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val db = EventDatabase.getInstance(this@EventsActivity)
            val eventsStored = withContext(Dispatchers.IO) { db.eventDao().getEvents() }
            val notesStored = withContext(Dispatchers.IO) { db.noteDao().getNotes() }
            events.clear(); events.addAll(eventsStored.map { it.toModel() })
            eventNotes.clear()
            notesStored.map { it.toModel() }.forEach { note ->
                note.attachmentUri?.takeIf { it.startsWith("event:") && note.content.isNotBlank() }?.let {
                    val eventId = it.removePrefix("event:").toLongOrNull()
                    if (eventId != null) eventNotes[eventId] = note
                }
            }
        }
    }

    @Composable
    override fun SegmentContent() {
        val events = remember { this@EventsActivity.events }
        val notes = remember { this@EventsActivity.eventNotes }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val showAttachDialog by showAttachDialogState
        val openEventId = remember { intent.getLongExtra(EXTRA_EVENT_ID, -1L).takeIf { it >= 0 } }
        LaunchedEffect(Unit) { intent.removeExtra(EXTRA_EVENT_ID) }
        EventsScreen(events, notes, openEventId)
        val attachable = events.filter { !notes.containsKey(it.id) }
        if (showAttachDialog) {
            AttachNoteDialog(
                events = attachable,
                onDismiss = { showAttachDialogState.value = false },
                onAttach = { event ->
                    showAttachDialogState.value = false
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).noteDao()
                        val now = System.currentTimeMillis()
                        val note = Note(
                            header = event.title,
                            content = "",
                            created = now,
                            lastOpened = now,
                            attachmentUri = "event:${event.id}"
                        )
                        val id = withContext(Dispatchers.IO) { dao.insert(note.toEntity()) }
                        val intent = Intent(context, NoteEditorActivity::class.java)
                        intent.putExtra(EXTRA_NOTE_ID, id)
                        intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                        intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                        intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                        intent.putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                        context.startActivity(intent)
                    }
                }
            )
        }
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        val context = LocalContext.current
        DropdownMenuItem(
            text = { Text("Attach Note") },
            onClick = {
                onDismiss()
                showAttachDialogState.value = true
            }
        )
        DropdownMenuItem(
            text = { Text("Trash") },
            onClick = {
                onDismiss()
                context.startActivity(android.content.Intent(context, EventTrashActivity::class.java))
            }
        )
    }
}

@Composable
private fun EventsScreen(events: MutableList<Event>, notes: MutableMap<Long, Note>, openEventId: Long?) {
    val context = LocalContext.current
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var pendingOpenEventId by remember { mutableStateOf(openEventId) }
    LaunchedEffect(events.size, pendingOpenEventId) {
        val id = pendingOpenEventId
        if (id != null) {
            val idx = events.indexOfFirst { it.id == id }
            if (idx >= 0) editingIndex = idx
            pendingOpenEventId = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(events) { index, event ->
                val hasNotification = event.hasValidNotificationTime()
                val accentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (hasNotification) {
                                it.border(
                                    width = 1.dp,
                                    color = accentColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                it
                            }
                        }
                        .clickable { editingIndex = index }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hasNotification) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(accentColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                val titleStyle = if (hasNotification) {
                                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                } else {
                                    MaterialTheme.typography.titleMedium
                                }
                                Text(
                                    text = "${index + 1}. ${event.title}",
                                    style = titleStyle
                                )
                            }
                            Icon(
                                imageVector = if (hasNotification) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                contentDescription = if (hasNotification) {
                                    "Notification enabled"
                                } else {
                                    "Notification disabled"
                                },
                                tint = if (hasNotification) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        event.getFormattedNotificationTime()?.let { time ->
                            Text(
                                text = "Reminder at $time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (event.description.isNotBlank()) {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        event.date?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        event.address?.takeIf { it.isNotBlank() }?.let { addr ->
                            Text(
                                text = addr,
                                color = Color.Blue,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        val uri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(addr))
                                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                        context.startActivity(mapIntent)
                                    }
                            )
                        }
                        notes[event.id]?.let { note ->
                            Text(
                                text = "Note attached",
                                color = Color(0xFF388E3C),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        val intent = Intent(context, NoteEditorActivity::class.java)
                                        intent.putExtra(EXTRA_NOTE_ID, note.id)
                                        intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                                        intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                                        intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                                        intent.putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                                        intent.putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                                        intent.putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                                        context.startActivity(intent)
                                    }
                            )
                        }
                    }
                }
            }
        }

        if (events.isEmpty()) {
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.events)
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
                Text(
                    text = "No events yet",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 68.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Event") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(
                        android.content.Intent(context, KizCalendarActivity::class.java)
                    )
                },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                text = { Text("Calendar") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        val index = editingIndex
        if (index != null) {
            val event = events[index]
            EventDialog(
                initial = event,
                onDismiss = { editingIndex = null },
                onSave = { title, desc, date, addr ->
                    editingIndex = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        val updated = Event(event.id, title, desc, date, addr)
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        events[index] = updated
                    }
                },
                onDelete = {
                    scope.launch {
                        val db = EventDatabase.getInstance(context)
                        withContext(Dispatchers.IO) {
                            db.trashedEventDao().insert(
                                TrashedEvent(
                                    title = event.title,
                                    description = event.description,
                                    date = event.date,
                                    address = event.address
                                ).toEntity()
                            )
                            db.eventDao().delete(event.toEntity())
                        }
                        events.removeAt(index)
                        editingIndex = null
                    }
                }
            )
        }

        if (showCreateDialog) {
            CreateEventDialog(
                onDismiss = { showCreateDialog = false },
                onCreated = { newEvent ->
                    events.add(newEvent)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
private fun AttachNoteDialog(
    events: List<Event>,
    onDismiss: () -> Unit,
    onAttach: (Event) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { if (selectedIndex >= 0) onAttach(events[selectedIndex]) },
                enabled = selectedIndex >= 0
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            if (events.isEmpty()) {
                Text("No events available")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    itemsIndexed(events) { index, event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIndex = index }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index }
                            )
                            Text(event.title, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        title = { Text("Attach Note") }
    )
}
