package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.DayPosition
import java.time.YearMonth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

/** Activity showing the Kizitonwose calendar. */
class KizCalendarActivity : SegmentActivity("Events Calendar") {
    @Composable
    override fun SegmentContent() {
        KizCalendarScreen()
    }
}

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
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val visibleMonth = state.firstVisibleMonth.yearMonth
        Text(
            text = "${visibleMonth.month.getDisplayName(TextStyle.FULL, locale)} ${visibleMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            for (day in daysOfWeekOrdered) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalCalendar(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            dayContent = { day ->
                val dayEvents = events.filter { it.date == day.date.toString() }
                val isCurrentMonth = day.position == DayPosition.MonthDate
                val textColor = when {
                    dayEvents.isNotEmpty() -> MaterialTheme.colorScheme.primary
                    isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .then(if (!isCurrentMonth) Modifier.alpha(0.5f) else Modifier)
                        .clickable(enabled = dayEvents.isNotEmpty()) {
                            selectedEvents = dayEvents
                            showDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (dayEvents.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .align(Alignment.BottomCenter)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Close") }
                },
                text = {
                    Column {
                        selectedEvents.forEach { event ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingEvent = event
                                        showDialog = false
                                    }
                            ) {
                                Text(event.title, style = MaterialTheme.typography.bodyLarge)
                                if (event.description.isNotBlank()) {
                                    Text(event.description, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
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
