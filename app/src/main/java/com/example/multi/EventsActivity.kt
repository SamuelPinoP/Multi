package com.example.multi

import android.os.Bundle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope

const val EXTRA_DATE = "extra_date"

/** Activity displaying the list of user events. */
class EventsActivity : SegmentActivity("Events") {
    private val events = mutableStateListOf<Event>()
    private var initialDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        initialDate = intent.getStringExtra(EXTRA_DATE)
        super.onCreate(savedInstanceState)
    }

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
        EventsScreen(events, initialDate)
        initialDate = null
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
private fun EventsScreen(events: MutableList<Event>, initialDate: String? = null) {
    val context = LocalContext.current
    var editingIndex by remember { mutableStateOf<Int?>(if (initialDate != null) -1 else null) }
    var newDate by remember { mutableStateOf(initialDate) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var reminderEditIndex by remember { mutableStateOf<Int?>(null) }
    var showReminderPicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(is24Hour = true)

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            IconButton(onClick = {
                                val current = events[index]
                                if (!current.reminderEnabled) {
                                    val updated = current.copy(reminderEnabled = true)
                                    events[index] = updated
                                    scope.launch {
                                        val dao = EventDatabase.getInstance(context).eventDao()
                                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                    }
                                    NotificationScheduler.schedule(context, updated)
                                }
                                val parts = events[index].reminderTime.split(":")
                                timeState.hour = parts[0].toInt()
                                timeState.minute = parts[1].toInt()
                                reminderEditIndex = index
                                showReminderPicker = true
                            }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (event.reminderEnabled) Color.Blue else Color.Gray
                                )
                            }
                            if (event.reminderEnabled) {
                                Text(event.reminderTime, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        if (events.isEmpty()) {
            val annotated = buildAnnotatedString {
                append("No events, ")
                pushStringAnnotation(tag = "ADD", annotation = "add")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("add")
                }
                pop()
                append(" some!")
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.text.ClickableText(
                    text = annotated,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Gray,
                        fontSize = 18.sp
                    ),
                    onClick = { offset ->
                        annotated.getStringAnnotations("ADD", offset, offset)
                            .firstOrNull()?.let {
                                newDate = null
                                editingIndex = -1
                            }
                    }
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
                    newDate = null
                    editingIndex = -1
                },
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
            val isNew = index < 0
            val event = if (isNew) Event(0L, "", "", null, null) else events[index]
            EventDialog(
                initial = event,
                onDismiss = {
                    editingIndex = null
                    newDate = null
                },
                onSave = { title, desc, date, addr, rem, time ->
                    editingIndex = null
                    newDate = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(Event(title = title, description = desc, date = date, address = addr, reminderEnabled = rem, reminderTime = time).toEntity())
                            }
                            val newEvent = Event(id, title, desc, date, addr, rem, time)
                            events.add(newEvent)
                            NotificationScheduler.schedule(context, newEvent)
                            snackbarHostState.showSnackbar("New Event added")
                        } else {
                            val updated = Event(event.id, title, desc, date, addr, rem, time)
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            events[index] = updated
                            if (rem) NotificationScheduler.schedule(context, updated) else NotificationScheduler.cancel(context, updated.id)
                        }
                    }
                },
                onDelete = if (isNew) null else {
                    {
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
                            newDate = null
                        }
                    }
                },
                isNew = isNew
            )
        }

        val remIndex = reminderEditIndex
        if (showReminderPicker && remIndex != null) {
            TimePickerDialog(
                onDismissRequest = { showReminderPicker = false; reminderEditIndex = null },
                confirmButton = {
                    TextButton(onClick = {
                        showReminderPicker = false
                        val e = events[remIndex]
                        val newTime = "%02d:%02d".format(timeState.hour, timeState.minute)
                        val updated = e.copy(reminderTime = newTime, reminderEnabled = true)
                        events[remIndex] = updated
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).eventDao()
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        }
                        NotificationScheduler.schedule(context, updated)
                        reminderEditIndex = null
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showReminderPicker = false; reminderEditIndex = null }) { Text("Cancel") }
                }
            ) {
                TimePicker(state = timeState)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp)
        )
    }
}

