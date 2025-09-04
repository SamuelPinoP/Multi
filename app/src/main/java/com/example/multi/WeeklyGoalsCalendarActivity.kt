package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.ui.theme.CalendarTodayBg
import com.example.multi.ui.theme.CalendarTodayBorder
import com.example.multi.EXTRA_GOAL_ID
import com.example.multi.util.capitalizeSentences
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

/** Activity showing weekly goals in a calendar view. */
class WeeklyGoalsCalendarActivity : SegmentActivity("Goals Calendar") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        WeeklyGoalsCalendarScreen()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyGoalsCalendarScreen() {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<WeeklyGoal>() }
    val completions = remember { mutableStateListOf<DailyCompletion>() }

    LaunchedEffect(Unit) {
        val db = EventDatabase.getInstance(context)
        val dao = db.weeklyGoalDao()
        val recordDao = db.weeklyGoalRecordDao()
        val completionDao = db.dailyCompletionDao()

        // Load goals (existing logic)
        val stored = withContext(Dispatchers.IO) { dao.getGoals() }
        val currentWeek = currentWeek()
        val today = LocalDate.now()
        val startCurrent = today.minusDays((today.dayOfWeek.value % 7).toLong())
        val prevStart = startCurrent.minusDays(7)
        val prevEnd = startCurrent.minusDays(1)
        val prevStartStr = prevStart.toString()
        val prevEndStr = prevEnd.toString()

        goals.clear()
        stored.forEach { entity ->
            var model = entity.toModel()
            if (model.weekNumber != currentWeek) {
                val completed = model.frequency - model.remaining
                val overage = (completed - model.frequency).coerceAtLeast(0).coerceAtMost(20)
                val record = WeeklyGoalRecord(
                    header = model.header,
                    completed = completed,
                    frequency = model.frequency,
                    weekStart = prevStartStr,
                    weekEnd = prevEndStr,
                    dayStates = model.dayStates,
                    overageCount = overage
                )
                withContext(Dispatchers.IO) { recordDao.insert(record.toEntity()) }
                model = model.copy(
                    remaining = model.frequency,
                    weekNumber = currentWeek,
                    lastCheckedDate = null,
                    dayStates = DEFAULT_DAY_STATES
                )
                withContext(Dispatchers.IO) { dao.update(model.toEntity()) }
            }
            val dayIndex = today.dayOfWeek.value % 7
            val chars = model.dayStates.toCharArray()
            var changed = false
            for (i in 0 until dayIndex) {
                if (chars[i] == '-') {
                    chars[i] = 'M'
                    changed = true
                }
            }
            if (changed) {
                model = model.copy(dayStates = String(chars))
                withContext(Dispatchers.IO) { dao.update(model.toEntity()) }
            }
            val completed = model.dayStates.count { it == 'C' }
            model = model.copy(remaining = model.frequency - completed)
            goals.add(model)
        }

        // Load completions for calendar display (12 months range)
        val startDate = today.minusMonths(12)
        val endDate = today.plusMonths(12)
        val loadedCompletions = withContext(Dispatchers.IO) {
            completionDao.getCompletionsInRange(startDate.toString(), endDate.toString())
        }
        completions.clear()
        completions.addAll(loadedCompletions.map { it.toModel() })
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

    var selectedCompletions by remember { mutableStateOf<List<DailyCompletion>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var editingGoal by remember { mutableStateOf<WeeklyGoal?>(null) }
    val scope = rememberCoroutineScope()
    var creatingGoal by remember { mutableStateOf(false) }

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
                    val completionsForDay = completions.filter {
                        it.completionDate == day.date.toString()
                    }

                    val isCurrentMonth = day.position == DayPosition.MonthDate
                    val textColor = when {
                        completionsForDay.isNotEmpty() -> MaterialTheme.colorScheme.primary
                        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val isToday = day.date == LocalDate.now()
                    val bgColor = when {
                        isToday -> CalendarTodayBg
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
                            .clickable(enabled = completionsForDay.isNotEmpty()) {
                                selectedCompletions = completionsForDay
                                showDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (isToday) MaterialTheme.colorScheme.onSurface else textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // Only show indicator if there are actual completions
                        if (completionsForDay.isNotEmpty()) {
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
                onClick = { creatingGoal = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Goal") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(android.content.Intent(context, WeeklyGoalsActivity::class.java))
                },
                icon = { Icon(Icons.Default.Flag, contentDescription = null) },
                text = { Text("My Goals") },
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
                    Text(
                        text = "Goals completed on ${selectedCompletions.firstOrNull()?.completionDate}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    selectedCompletions.forEach { completion ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    val intent = android.content.Intent(
                                        context,
                                        WeeklyGoalsActivity::class.java
                                    )
                                    intent.putExtra(EXTRA_GOAL_ID, completion.goalId)
                                    context.startActivity(intent)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(completion.goalHeader, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Completed on ${completion.completionDate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

        if (creatingGoal) {
            WeeklyGoalDialog(
                initial = WeeklyGoal(header = "", frequency = 1),
                onDismiss = { creatingGoal = false },
                onSave = { header, freq ->
                    creatingGoal = false
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        val id = withContext(Dispatchers.IO) {
                            dao.insert(WeeklyGoal(header = header, frequency = freq).toEntity())
                        }
                        goals.add(WeeklyGoal(id, header, freq))
                        context.startActivity(android.content.Intent(context, WeeklyGoalsActivity::class.java))
                    }
                },
                isNew = true
            )
        }

        editingGoal?.let { goal ->
            WeeklyGoalDialog(
                initial = goal,
                onDismiss = { editingGoal = null },
                onSave = { header, freq ->
                    editingGoal = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        val updated = goal.copy(header = header, frequency = freq)
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        val idx = goals.indexOfFirst { it.id == goal.id }
                        if (idx >= 0) {
                            goals[idx] = updated
                        }
                    }
                },
                onDelete = {
                    editingGoal = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        withContext(Dispatchers.IO) { dao.delete(goal.toEntity()) }
                        val idx = goals.indexOfFirst { it.id == goal.id }
                        if (idx >= 0) {
                            goals.removeAt(idx)
                        }
                    }
                },
                isNew = false
            )
        }
    }
}

@Composable
private fun WeeklyGoalDialog(
    initial: WeeklyGoal,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    isNew: Boolean = false
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
            Row {
                onDelete?.let { del ->
                    TextButton(onClick = del) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
        title = { Text("Weekly Goal") },
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