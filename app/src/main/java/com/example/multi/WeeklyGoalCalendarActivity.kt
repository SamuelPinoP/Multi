package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Activity showing weekly goal completions on a calendar. */
class WeeklyGoalCalendarActivity : SegmentActivity("Weekly Goals View") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        WeeklyGoalCalendarScreen()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyGoalCalendarScreen() {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<WeeklyGoal>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
        val stored = withContext(Dispatchers.IO) { dao.getGoals() }
        goals.clear()
        goals.addAll(stored.map { it.toModel() })
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

    val today = LocalDate.now()
    val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
    val completions = remember(goals) {
        val map = mutableMapOf<LocalDate, MutableList<String>>()
        goals.forEach { goal ->
            goal.dayStates.forEachIndexed { idx, c ->
                if (c == 'C') {
                    val date = startOfWeek.plusDays(idx.toLong())
                    map.getOrPut(date) { mutableListOf() }.add(goal.header)
                }
            }
        }
        map
    }

    var selectedHeaders by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var addingGoal by remember { mutableStateOf(false) }
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
                    scope.launch { state.animateScrollToMonth(visibleMonth.plusMonths(1)) }
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
                    val headers = completions[day.date] ?: emptyList()
                    val isCurrentMonth = day.position == DayPosition.MonthDate
                    val textColor = when {
                        headers.isNotEmpty() -> MaterialTheme.colorScheme.primary
                        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val isToday = day.date == today
                    val bgColor = when {
                        isToday -> CalendarTodayBg
                        headers.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer
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
                            .clickable(enabled = headers.isNotEmpty()) {
                                selectedHeaders = headers
                                showDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (isToday) MaterialTheme.colorScheme.onSurface else textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (headers.isNotEmpty()) {
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
                onClick = { addingGoal = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Weekly Goal") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(android.content.Intent(context, WeeklyGoalsActivity::class.java))
                },
                icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                text = { Text("My Weekly Goals") },
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
                    selectedHeaders.forEach { header ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(header, style = MaterialTheme.typography.titleMedium)
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

        if (addingGoal) {
            WeeklyGoalDialog(
                initial = WeeklyGoal(header = "", frequency = 1),
                onDismiss = { addingGoal = false },
                onSave = { header, freq ->
                    addingGoal = false
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        val id = withContext(Dispatchers.IO) {
                            dao.insert(WeeklyGoal(header = header, frequency = freq).toEntity())
                        }
                        goals.add(WeeklyGoal(id, header, freq))
                    }
                }
            )
        }
    }
}

@Composable
private fun WeeklyGoalDialog(
    initial: WeeklyGoal,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var header by remember { mutableStateOf(initial.header) }
    var frequency by remember { mutableStateOf<Int?>(initial.frequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { frequency?.let { onSave(header, it) } },
                enabled = header.isNotBlank() && frequency != null
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Customize your Weekly Routine!") },
        text = {
            Column {
                Text("Header", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it.capitalizeSentences() },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Frequency", style = MaterialTheme.typography.bodySmall)
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .fillMaxWidth()
                ) {
                    for (i in 1..7) {
                        val text = when (i) {
                            1 -> "Once a Week"
                            2 -> "Twice a Week"
                            7 -> "Every Day"
                            else -> "$i Times a Week"
                        }
                        val selected = frequency == i
                        val selectedColor = MaterialTheme.colorScheme.primary
                        val unselectedColor = MaterialTheme.colorScheme.surfaceVariant

                        Button(
                            onClick = { frequency = i },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) selectedColor else unselectedColor
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text)
                        }
                    }
                }
            }
        }
    )
}
