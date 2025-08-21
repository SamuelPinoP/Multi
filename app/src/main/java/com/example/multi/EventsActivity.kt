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
import androidx.compose.material.icons.filled.AttachFile
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import android.widget.Toast

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
    var attachMode by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val notePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val noteId = result.data?.getLongExtra(EXTRA_SELECTED_NOTE_ID, 0L) ?: 0L
        val idx = selectedIndex
        if (result.resultCode == Activity.RESULT_OK && idx != null && noteId != 0L) {
            val current = events[idx]
            val updated = current.copy(noteId = noteId)
            scope.launch {
                val dao = EventDatabase.getInstance(context).eventDao()
                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                events[idx] = updated
                Toast.makeText(context, "Note attached", Toast.LENGTH_SHORT).show()
            }
        }
        attachMode = false
        selectedIndex = null
    }

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
                        .clickable {
                            if (attachMode) {
                                selectedIndex = index
                                val intent = Intent(context, NotesActivity::class.java)
                                intent.putExtra(EXTRA_PICK_NOTE, true)
                                notePickerLauncher.launch(intent)
                            } else {
                                editingIndex = index
                            }
                        }
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
                                color = Color(0xFF388E3C),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable {
                                        scope.launch {
                                            val dao = EventDatabase.getInstance(context).noteDao()
                                            val note = withContext(Dispatchers.IO) { dao.getNoteById(noteId) }
                                            note?.let { n ->
                                                val intent = Intent(context, NoteEditorActivity::class.java).apply {
                                                    putExtra(EXTRA_NOTE_ID, n.id)
                                                    putExtra(EXTRA_NOTE_HEADER, n.header)
                                                    putExtra(EXTRA_NOTE_CONTENT, n.content)
                                                    putExtra(EXTRA_NOTE_CREATED, n.created)
                                                    putExtra(EXTRA_NOTE_SCROLL, n.scroll)
                                                    putExtra(EXTRA_NOTE_CURSOR, n.cursor)
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
                onClick = {
                    attachMode = true
                    Toast.makeText(context, "Choose an event", Toast.LENGTH_SHORT).show()
                },
                icon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
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
                        val updated = event.copy(title = title, description = desc, date = date, address = addr)
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

