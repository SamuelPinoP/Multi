package com.example.multi

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
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
import java.time.LocalDate
import java.time.ZoneId

const val EXTRA_DATE = "extra_date"

/** Activity displaying the list of user events. */
class EventsActivity : SegmentActivity("Events") {
    private val events = mutableStateListOf<Event>()
    private var initialDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        initialDate = intent.getStringExtra(EXTRA_DATE)
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermission()
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
        EventsScreen(events, initialDate) { event ->
            scheduleNotification(this@EventsActivity, event)
        }
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "events_channel",
                "Event Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }
    }

    private fun scheduleNotification(context: Context, event: Event) {
        val date = event.date ?: return
        runCatching {
            val triggerTime = LocalDate.parse(date)
                .atTime(9, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", event.title)
                putExtra("desc", event.description)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                event.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        }
    }
}

@Composable
private fun EventsScreen(
    events: MutableList<Event>,
    initialDate: String? = null,
    onNotify: (Event) -> Unit
) {
    val context = LocalContext.current
    var editingIndex by remember { mutableStateOf<Int?>(if (initialDate != null) -1 else null) }
    var newDate by remember { mutableStateOf(initialDate) }
    val snackbarHostState = remember { SnackbarHostState() }
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
                        Button(
                            onClick = { onNotify(event) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) { Text("Notify") }
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
                onSave = { title, desc, date, addr ->
                    editingIndex = null
                    newDate = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(Event(title = title, description = desc, date = date, address = addr).toEntity())
                            }
                            events.add(Event(id, title, desc, date, addr))
                            snackbarHostState.showSnackbar("New Event added")
                        } else {
                            val updated = Event(event.id, title, desc, date, addr)
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            events[index] = updated
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp)
        )
    }
}

