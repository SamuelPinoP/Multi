package com.example.multi


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import com.example.multi.Note
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
            val dao = EventDatabase.getInstance(this@EventsActivity).eventDao()
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            events.clear(); events.addAll(stored.map { it.toModel() })
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
    var showAttachDialog by remember { mutableStateOf(false) }
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
                        .clickable { editingIndex = index }
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
                        event.noteId?.let { noteId ->
                            Text(
                                text = "Note attached",
                                color = Color.Green,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        scope.launch {
                                            val note = withContext(Dispatchers.IO) {
                                                EventDatabase.getInstance(context).noteDao().getNote(noteId)
                                            }
                                            note?.let {
                                                val intent = android.content.Intent(context, NoteEditorActivity::class.java).apply {
                                                    putExtra(EXTRA_NOTE_ID, it.id)
                                                    putExtra(EXTRA_NOTE_HEADER, it.header)
                                                    putExtra(EXTRA_NOTE_CONTENT, it.content)
                                                    putExtra(EXTRA_NOTE_CREATED, it.created)
                                                    putExtra(EXTRA_NOTE_SCROLL, it.scroll)
                                                    putExtra(EXTRA_NOTE_CURSOR, it.cursor)
                                                }
                                                context.startActivity(intent)
                                            }
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
                onClick = { showAttachDialog = true },
                icon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
                text = { Text("Attach Note") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
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
                        val updated = Event(event.id, title, desc, date, addr, event.noteId)
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

        if (showAttachDialog) {
            AttachNoteDialog(
                events = events,
                onDismiss = { showAttachDialog = false },
                onAttach = { selectedIndex ->
                    val event = events[selectedIndex]
                    scope.launch {
                        val db = EventDatabase.getInstance(context)
                        val note = Note(
                            header = event.title,
                            content = "",
                            created = System.currentTimeMillis(),
                            lastOpened = System.currentTimeMillis()
                        )
                        val noteId = withContext(Dispatchers.IO) { db.noteDao().insert(note.toEntity()) }
                        val updated = event.copy(noteId = noteId)
                        withContext(Dispatchers.IO) { db.eventDao().update(updated.toEntity()) }
                        events[selectedIndex] = updated
                    }
                    showAttachDialog = false
                }
            )
        }

    }
}

@Composable
private fun AttachNoteDialog(
    events: List<Event>,
    onDismiss: () -> Unit,
    onAttach: (Int) -> Unit
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { selectedIndex?.let { onAttach(it) } },
                enabled = selectedIndex != null
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = {
            Column {
                events.forEachIndexed { index, event ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedIndex = index }
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event.title)
                    }
                }
            }
        },
        title = { Text("Select Event") }
    )
}

