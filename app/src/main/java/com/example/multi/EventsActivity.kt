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
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import android.app.TimePickerDialog

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
    var bellEditingIndex by remember { mutableStateOf<Int?>(null) }

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
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val color = if (event.reminderTime != null) Color.Blue else MaterialTheme.colorScheme.onSurface
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Reminder",
                                    tint = color,
                                    modifier = Modifier.clickable { bellEditingIndex = index }
                                )
                                event.reminderTime?.let { rt ->
                                    val time = java.time.Instant.ofEpochMilli(rt).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                                    Text(
                                        text = String.format("%02d:%02d", time.hour, time.minute),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
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
            val event = if (isNew) Event(0L, "", "", null, null, null) else events[index]
            EventDialog(
                initial = event,
                onDismiss = {
                    editingIndex = null
                    newDate = null
                },
                onSave = { title, desc, date, addr, reminder ->
                    editingIndex = null
                    newDate = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(Event(title = title, description = desc, date = date, address = addr, reminderTime = reminder).toEntity())
                            }
                            val newEvent = Event(id, title, desc, date, addr, reminder)
                            events.add(newEvent)
                            EventReminderScheduler.schedule(context, newEvent)
                            snackbarHostState.showSnackbar("New Event added")
                        } else {
                            val updated = Event(event.id, title, desc, date, addr, reminder)
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            events[index] = updated
                            if (reminder != null) {
                                EventReminderScheduler.schedule(context, updated)
                            } else {
                                EventReminderScheduler.cancel(context, updated.id)
                            }
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
                                        address = event.address,
                                        reminderTime = event.reminderTime
                                    ).toEntity()
                                )
                                db.eventDao().delete(event.toEntity())
                            }
                            EventReminderScheduler.cancel(context, event.id)
                            events.removeAt(index)
                            editingIndex = null
                            newDate = null
                        }
                    }
                },
                isNew = isNew
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp)
        )

        bellEditingIndex?.let { idx ->
            val event = events[idx]
            LaunchedEffect(idx) {
                val cal = java.util.Calendar.getInstance()
                event.reminderTime?.let { cal.timeInMillis = it }
                TimePickerDialog(
                    context,
                    { _: android.widget.TimePicker, h: Int, m: Int ->
                        val date = event.date?.let { d ->
                            try { java.time.LocalDate.parse(d) } catch (_: Exception) { null }
                        } ?: java.time.LocalDate.now()
                        val instant = date.atTime(h, m).atZone(java.time.ZoneId.systemDefault()).toInstant()
                        val updated = event.copy(reminderTime = instant.toEpochMilli())
                        events[idx] = updated
                        EventReminderScheduler.schedule(context, updated)
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).eventDao()
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        }
                        bellEditingIndex = null
                    },
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE),
                    false
                ).apply { setOnCancelListener { bellEditingIndex = null } }.show()
            }
        }
    }
}

