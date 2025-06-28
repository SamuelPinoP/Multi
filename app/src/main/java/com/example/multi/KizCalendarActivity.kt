package com.example.multi

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
import java.time.DayOfWeek
import java.time.format.TextStyle
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
import com.example.multi.ui.theme.CalendarBlue
import com.example.multi.ui.theme.CalendarMonthBg
import com.example.multi.ui.theme.CalendarTodayBg
import com.example.multi.ui.theme.CalendarTodayBorder

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
    var creatingEvent by remember { mutableStateOf(false) }

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
                        if (isCurrentMonthVisible) CalendarMonthBg else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${visibleMonth.month.getDisplayName(TextStyle.FULL, locale)} ${visibleMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = if (isCurrentMonthVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
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
                .background(CalendarBlue, RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp)
        ) {
            for (day in daysOfWeekOrdered) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        HorizontalCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            state = state,
            dayContent = { day ->
                val dayEvents = events.filter { it.date == day.date.toString() }
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
                onClick = { creatingEvent = true },
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
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingEvent = event
                                    showDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(event.title, style = MaterialTheme.typography.titleMedium)
                                if (event.description.isNotBlank()) {
                                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
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

        if (creatingEvent) {
            EventDialog(
                initial = Event(0L, "", ""),
                onDismiss = { creatingEvent = false },
                onSave = { title, desc, date ->
                    creatingEvent = false
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        val id = withContext(Dispatchers.IO) {
                            dao.insert(Event(title = title, description = desc, date = date).toEntity())
                        }
                        events.add(Event(id, title, desc, date))
                        context.startActivity(android.content.Intent(context, EventsActivity::class.java))
                    }
                }
            )
        }

        editingEvent?.let { event ->
            EventDialog(
                initial = event,
                onDismiss = { editingEvent = null },
                onSave = { title, desc, date ->
                    editingEvent = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        val updated = Event(event.id, title, desc, date)
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        val idx = events.indexOfFirst { it.id == event.id }
                        if (idx >= 0) {
                            events[idx] = updated
                        }
                    }
                },
                onDelete = {
                    editingEvent = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        withContext(Dispatchers.IO) { dao.delete(event.toEntity()) }
                        val idx = events.indexOfFirst { it.id == event.id }
                        if (idx >= 0) {
                            events.removeAt(idx)
                        }
                    }
                }
            )
        }
    }
}
