package com.example.multi

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.YearMonth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.data.toEntity
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.graphics.Color
import com.example.multi.ui.components.NotificationBellMenu
import com.example.multi.ui.theme.CalendarTodayBg
import com.example.multi.ui.theme.CalendarTodayBorder
import com.example.multi.util.occursOn
import com.example.multi.util.showModernToast

/** Activity showing the Kizitonwose calendar. */
class KizCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        KizCalendarScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KizCalendarScreen() {
    val context = LocalContext.current
    val events = remember { mutableStateListOf<Event>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        events.clear()
        events.addAll(stored.map { it.toModel() }.filter { it.date != null })
    }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = firstDayOfWeekFromLocale()
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val locale = Locale.getDefault()
    val daysOfWeek = remember {
        DayOfWeek.entries.toList()
    }
    val daysOfWeekOrdered = remember(firstDayOfWeek) {
        val startIndex = firstDayOfWeek.ordinal
        daysOfWeek.drop(startIndex) + daysOfWeek.take(startIndex)
    }

    var selectedEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }

    fun updateEventCaches(updated: Event) {
        val idx = events.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            events[idx] = updated
        }
        selectedEvents = selectedEvents.map { if (it.id == updated.id) updated else it }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        val visibleMonth = state.firstVisibleMonth.yearMonth
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            IconButton(onClick = {
                scope.launch {
                    state.animateScrollToMonth(visibleMonth.minusMonths(1))
                }
            }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }

            val isCurrentMonthVisible = visibleMonth == currentMonth
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(
                        if (isCurrentMonthVisible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${visibleMonth.month.getDisplayName(TextStyle.FULL, locale)} ${visibleMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = if (isCurrentMonthVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = {
                scope.launch {
                    state.animateScrollToMonth(visibleMonth.plusMonths(1))
                }
            }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                .padding(vertical = 6.dp)
        ) {
            for (day in daysOfWeekOrdered) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        HorizontalCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            state = state,
            dayContent = { day ->
                val dayEvents = events.filter { it.occursOn(day.date) }
                val isCurrentMonth = day.position == DayPosition.MonthDate
                val textColor = when {
                    dayEvents.isNotEmpty() -> MaterialTheme.colorScheme.primary
                    isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val isToday = day.date == java.time.LocalDate.now()
                val bgColor = when {
                    isToday -> CalendarTodayBg
                    dayEvents.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .then(
                            when {
                                isToday -> Modifier.border(
                                    width = 2.dp,
                                    color = CalendarTodayBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                isCurrentMonth -> Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                else -> Modifier
                            }
                        )
                        .background(bgColor, RoundedCornerShape(8.dp))
                        .then(if (!isCurrentMonth) Modifier.alpha(0.5f) else Modifier)
                        .clickable(enabled = dayEvents.isNotEmpty()) {
                            selectedEvents = dayEvents
                            showDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = if (isToday) MaterialTheme.colorScheme.onSurface else textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (dayEvents.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
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
                    context.startActivity(android.content.Intent(context, EventsActivity::class.java))
                },
                icon = { Icon(Icons.Default.Event, contentDescription = null) },
                text = { Text("My Events") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (showDialog) {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedEvents.forEach { event ->
                        val hasNotification = event.notificationEnabled && event.getFormattedNotificationTime() != null
                        val accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (hasNotification) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = accentColor,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable {
                                    editingEvent = event
                                    showDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val titleStyle = MaterialTheme.typography.titleMedium
                                val formattedTime = event.getFormattedNotificationTime()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        event.title,
                                        style = if (hasNotification) {
                                            titleStyle.copy(fontWeight = FontWeight.SemiBold)
                                        } else {
                                            titleStyle
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (hasNotification) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                            )
                                        }
                                        NotificationBellMenu(
                                            hasNotification = hasNotification,
                                            onAddNotification = {
                                                val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                                    context.startActivity(android.content.Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                                    context.showModernToast("Please grant 'Alarms & reminders' permission to schedule exact notifications.")
                                                } else {
                                                    val calendar = Calendar.getInstance()
                                                    val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
                                                    val initialMinute = calendar.get(Calendar.MINUTE)
                                                    TimePickerDialog(
                                                        context,
                                                        { _, selectedHour, selectedMinute ->
                                                            scope.launch {
                                                                val updated = event.copy()
                                                                updated.setNotificationTime(selectedHour, selectedMinute)
                                                                val success = scheduleEventNotification(
                                                                    context,
                                                                    updated.title,
                                                                    updated.description,
                                                                    selectedHour,
                                                                    selectedMinute,
                                                                    updated.date
                                                                )
                                                                if (success) {
                                                                    val dao = EventDatabase.getInstance(context).eventDao()
                                                                    withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                                                    updateEventCaches(updated)
                                                                    context.showModernToast(
                                                                        "Notification scheduled for ${String.format("%02d:%02d", selectedHour, selectedMinute)}"
                                                                    )
                                                                } else {
                                                                    context.showModernToast("Failed to schedule notification")
                                                                }
                                                            }
                                                        },
                                                        initialHour,
                                                        initialMinute,
                                                        true
                                                    ).show()
                                                }
                                            },
                                            onRemoveNotification = {
                                                scope.launch {
                                                    val updated = event.copy().apply { disableNotification() }
                                                    val dao = EventDatabase.getInstance(context).eventDao()
                                                    withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                                    updateEventCaches(updated)
                                                    context.showModernToast("Notification removed")
                                                }
                                            },
                                            onEditNotification = {
                                                val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as AlarmManager
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                                    context.startActivity(android.content.Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                                    context.showModernToast("Please grant 'Alarms & reminders' permission to schedule exact notifications.")
                                                } else {
                                                    val calendar = Calendar.getInstance()
                                                    val initialHour = event.notificationHour ?: calendar.get(Calendar.HOUR_OF_DAY)
                                                    val initialMinute = event.notificationMinute ?: calendar.get(Calendar.MINUTE)
                                                    TimePickerDialog(
                                                        context,
                                                        { _, selectedHour, selectedMinute ->
                                                            scope.launch {
                                                                val updated = event.copy()
                                                                updated.setNotificationTime(selectedHour, selectedMinute)
                                                                val success = scheduleEventNotification(
                                                                    context,
                                                                    updated.title,
                                                                    updated.description,
                                                                    selectedHour,
                                                                    selectedMinute,
                                                                    updated.date
                                                                )
                                                                if (success) {
                                                                    val dao = EventDatabase.getInstance(context).eventDao()
                                                                    withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                                                    updateEventCaches(updated)
                                                                    context.showModernToast(
                                                                        "Notification updated for ${String.format("%02d:%02d", selectedHour, selectedMinute)}"
                                                                    )
                                                                } else {
                                                                    context.showModernToast("Failed to schedule notification")
                                                                }
                                                            }
                                                        },
                                                        initialHour,
                                                        initialMinute,
                                                        true
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        formattedTime?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                if (event.description.isNotBlank()) {
                                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                                }
                                event.date?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall)
                                }
                                event.address?.takeIf { it.isNotBlank() }?.let { addr ->
                                    Text(
                                        addr,
                                        color = Color.Blue,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable {
                                                val uri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(addr))
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                                context.startActivity(intent)
                                            }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Close") }
                }
            }
        }

        if (showCreateDialog) {
            CreateEventDialog(
                onDismiss = { showCreateDialog = false },
                onCreated = { event ->
                    if (event.date != null) {
                        events.add(event)
                    }
                    showCreateDialog = false
                }
            )
        }

        editingEvent?.let { event ->
            EventDialog(
                initial = event,
                onDismiss = { editingEvent = null },
                onSave = { title, desc, date, addr ->
                    editingEvent = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        val updated = event.copy(
                            title = title,
                            description = desc,
                            date = date,
                            address = addr
                        )
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        updateEventCaches(updated)
                    }
                },
                onDelete = {
                    editingEvent = null
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
                        val idx = events.indexOfFirst { it.id == event.id }
                        if (idx >= 0) {
                            events.removeAt(idx)
                        }
                        selectedEvents = selectedEvents.filterNot { it.id == event.id }
                    }
                }
            )
        }
    }
}
