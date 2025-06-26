package com.example.multi

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase

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
    val eventDates = remember { mutableStateListOf<LocalDate>() }
    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val events = withContext(Dispatchers.IO) { dao.getEvents() }
        eventDates.clear()
        events.forEach { e ->
            e.date?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()?.let { d ->
                    eventDates.add(d)
                }
            }
        }
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
                val hasEvent = eventDates.contains(day.date)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .background(
                            color = if (hasEvent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                        .clickable(enabled = hasEvent) {
                            val intent = Intent(context, EventsActivity::class.java)
                            intent.putExtra(EXTRA_DATE, day.date.toString())
                            context.startActivity(intent)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = if (hasEvent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}
