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
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

/** Activity showing weekly goal completions on a calendar. */
class WeeklyRecordCalendarActivity : SegmentActivity("Weekly Goals Calendar") {
    @Composable
    override fun SegmentContent() {
        WeeklyRecordCalendarScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyRecordCalendarScreen() {
    val context = LocalContext.current
    val records = remember { mutableStateListOf<WeeklyGoalRecord>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyGoalRecordDao()
        val stored = withContext(Dispatchers.IO) { dao.getRecords() }
        records.clear()
        records.addAll(stored.map { it.toModel() })
    }

    // Map of date -> list of completed goal headers
    val completed = remember { mutableMapOf<LocalDate, MutableList<String>>() }
    LaunchedEffect(records) {
        completed.clear()
        records.forEach { rec ->
            val start = LocalDate.parse(rec.weekStart)
            rec.dayStates.forEachIndexed { idx, c ->
                if (c == 'C') {
                    val date = start.plusDays(idx.toLong())
                    completed.getOrPut(date) { mutableListOf() }.add(rec.header)
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
    val daysOfWeek = remember { DayOfWeek.entries.toList() }
    val daysOfWeekOrdered = remember(firstDayOfWeek) {
        val startIndex = firstDayOfWeek.ordinal
        daysOfWeek.drop(startIndex) + daysOfWeek.take(startIndex)
    }

    var selectedHeaders by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                    scope.launch { state.animateScrollToMonth(visibleMonth.minusMonths(1)) }
                }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month") }

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
                    scope.launch { state.animateScrollToMonth(visibleMonth.plusMonths(1)) }
                }) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next month") }
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
                    val dayGoals = completed[day.date]
                    val isCurrentMonth = day.position == DayPosition.MonthDate
                    val isToday = day.date == LocalDate.now()
                    val textColor = when {
                        dayGoals != null -> Color(0xFF388E3C)
                        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val bgColor = when {
                        isToday -> CalendarTodayBg
                        dayGoals != null -> Color(0xFFC8E6C9)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .then(
                                when {
                                    isToday -> Modifier.border(2.dp, CalendarTodayBorder, RoundedCornerShape(8.dp))
                                    isCurrentMonth -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    else -> Modifier
                                }
                            )
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .then(if (!isCurrentMonth) Modifier.alpha(0.5f) else Modifier)
                            .clickable(enabled = dayGoals != null) {
                                selectedHeaders = dayGoals ?: emptyList()
                                showDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (isToday) MaterialTheme.colorScheme.onSurface else textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (dayGoals != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                        }
                    }
                }
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
                    selectedHeaders.forEach { header ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(12.dp)
                            )
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
    }
}

