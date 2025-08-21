package com.example.multi


import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

/** Activity displaying the list of user events. */
class EventsActivity : SegmentActivity("Events") {
    private val events = mutableStateListOf<Event>()

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val db = EventDatabase.getInstance(this@EventsActivity)
            val eventDao = db.eventDao()
            val noteDao = db.noteDao()
            val stored = withContext(Dispatchers.IO) { eventDao.getEvents() }
            val notes = withContext(Dispatchers.IO) { noteDao.getNotes() }
            val attached = notes.mapNotNull { note ->
                note.attachmentUri?.takeIf { it.startsWith("event:") }?.substringAfter("event:")?.toLongOrNull()?.let { it to note.id }
            }.toMap()
            events.clear()
            stored.map { it.toModel() }.forEach { e ->
                e.attachedNoteId = attached[e.id]
                events.add(e)
            }
        }
    }

    @Composable
    override fun SegmentContent() {
        val events = remember { this@EventsActivity.events }
        EventsScreen(events)
    }

    @Composable
    override fun OverflowMenuItems(onDismiss: () -> Unit) {
        val context = LocalContext.current
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
private fun EventsScreen(events: MutableList<Event>) {
    val context = LocalContext.current
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var attachIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(events) { index, event ->
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { editingIndex = index },
                            onLongClick = {
                                if (events[index].attachedNoteId == null) {
                                    attachIndex = index
                                }
                            }
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${index + 1}. ${event.title}",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                        if (event.attachedNoteId != null) {
                            Text(
                                text = "Note attached",
                                color = Color(0xFF2E7D32),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        val noteId = event.attachedNoteId ?: return@clickable
                                        scope.launch {
                                            val dao = EventDatabase.getInstance(context).noteDao()
                                            val note = withContext(Dispatchers.IO) { dao.getById(noteId) } ?: return@launch
                                            val intent = android.content.Intent(context, NoteEditorActivity::class.java)
                                            intent.putExtra(EXTRA_NOTE_ID, note.id)
                                            intent.putExtra(EXTRA_NOTE_HEADER, note.header)
                                            intent.putExtra(EXTRA_NOTE_CONTENT, note.content)
                                            intent.putExtra(EXTRA_NOTE_CREATED, note.created)
                                            intent.putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                                            intent.putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                                            context.startActivity(intent)
                                        }
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
                        val updated = Event(
                            id = event.id,
                            title = title,
                            description = desc,
                            date = date,
                            address = addr,
                            notificationHour = event.notificationHour,
                            notificationMinute = event.notificationMinute,
                            notificationEnabled = event.notificationEnabled,
                            attachedNoteId = event.attachedNoteId
                        )
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

        val attach = attachIndex
        if (attach != null) {
            AlertDialog(
                onDismissRequest = { attachIndex = null },
                confirmButton = {
                    TextButton(onClick = {
                        val event = events[attach]
                        attachIndex = null
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).noteDao()
                            val note = Note(
                                header = event.title,
                                content = "",
                                attachmentUri = "event:${event.id}"
                            )
                            val id = withContext(Dispatchers.IO) { dao.insert(note.toEntity()) }
                            events[attach] = event.copy(attachedNoteId = id)
                        }
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { attachIndex = null }) { Text("Cancel") } },
                text = { Text("Attach a note to ${events[attach].title}?") }
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

