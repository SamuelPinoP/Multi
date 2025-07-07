package com.example.multi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
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
import androidx.compose.ui.Modifier
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import androidx.compose.ui.graphics.Color
import com.example.multi.ui.theme.CalendarTodayBg
import com.example.multi.ui.theme.CalendarTodayBorder
import java.time.LocalDate

/** Activity showing weekly goal completion calendar. */
class WeeklyGoalsCalendarActivity : SegmentActivity("Weekly Goals View") {
    @Composable
    override fun SegmentContent() {
        WeeklyGoalsCalendarScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyGoalsCalendarScreen() {
    val context = LocalContext.current
    var completedMap by remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val db = EventDatabase.getInstance(context)
        val recDao = db.weeklyGoalRecordDao()
        val goalDao = db.weeklyGoalDao()
        val records = withContext(Dispatchers.IO) { recDao.getRecords() }
        val goals = withContext(Dispatchers.IO) { goalDao.getGoals() }
        val map = mutableMapOf<LocalDate, MutableList<String>>()
        records.map { it.toModel() }.forEach { rec ->
            val start = LocalDate.parse(rec.weekStart)
            rec.dayStates.forEachIndexed { idx, c ->
                if (c == 'C') {
                    val date = start.plusDays(idx.toLong())
                    map.getOrPut(date) { mutableListOf() }.add(rec.header)
                }
            }
        }
        val today = LocalDate.now()
        val startCurrent = today.minusDays((today.dayOfWeek.value % 7).toLong())
        goals.map { it.toModel() }.forEach { goal ->
            goal.dayStates.forEachIndexed { idx, c ->
                if (c == 'C') {
                    val date = startCurrent.plusDays(idx.toLong())
                    map.getOrPut(date) { mutableListOf() }.add(goal.header)
                }
            }
        }
        completedMap = map.mapValues { it.value.toList() }
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
                    val headers = completedMap[day.date]
                    val isCurrentMonth = day.position == DayPosition.MonthDate
                    val isToday = day.date == LocalDate.now()
                    val bgColor = when {
                        isToday -> CalendarTodayBg
                        headers != null -> Color(0xFF4CAF50)
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        headers != null -> Color.White
                        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                            .clickable(enabled = headers != null) {
                                headers?.let {
                                    selectedHeaders = it
                                    showDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (isToday) MaterialTheme.colorScheme.onSurface else textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (headers != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.White, CircleShape)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                style = MaterialTheme.typography.titleMedium
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

