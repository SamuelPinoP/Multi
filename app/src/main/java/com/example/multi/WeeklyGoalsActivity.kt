package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import com.example.multi.util.capitalizeSentences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

const val EXTRA_GOAL_ID = "extra_goal_id"

class WeeklyGoalsActivity : SegmentActivity("Weekly Goals") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)
        WeeklyGoalsScreen(highlightGoalId = goalId.takeIf { it > 0 })
    }
}

@Composable
private fun DayChoiceDialog(
    onDismiss: () -> Unit,
    onMiss: () -> Unit,
    onComplete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onComplete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Icon(Icons.Default.Check, contentDescription = null) }
        },
        dismissButton = {
            Button(
                onClick = {
                    onMiss()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Icon(Icons.Default.Close, contentDescription = null) }
        },
        title = { Text("Mark Day") },
        text = { Text("Choose status") }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeeklyGoalsScreen(highlightGoalId: Long? = null) {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<WeeklyGoal>() }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedGoalIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(highlightGoalId) {
        val db = EventDatabase.getInstance(context)
        val dao = db.weeklyGoalDao()
        val recordDao = db.weeklyGoalRecordDao()
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
                val completed = model.dayStates.count { it == 'C' }
                val record = WeeklyGoalRecord(
                    header = model.header,
                    completed = completed,
                    frequency = model.frequency,
                    weekStart = prevStartStr,
                    weekEnd = prevEndStr,
                    dayStates = model.dayStates
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
            val completed = model.dayStates.count { it == 'C' }
            model = model.copy(remaining = (model.frequency - completed).coerceAtLeast(0))
            goals.add(model)
        }
        highlightGoalId?.let { id ->
            val index = goals.indexOfFirst { it.id == id }
            if (index >= 0) {
                editingIndex = index
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ElevatedButton(
                    onClick = {
                        context.startActivity(
                            android.content.Intent(context, RecordActivity::class.java)
                        )
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .defaultMinSize(minWidth = 170.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Record", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val remaining = daysRemainingInWeek()
            Text(
                text = "$remaining days remaining",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Today's Goals",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                itemsIndexed(goals) { index, goal ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingIndex = index }
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            if (goal.remaining == 0) {
                                Text(
                                    text = "Completed!",
                                    color = Color.Green,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.header,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${goal.frequency - goal.remaining}/${goal.frequency}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    val today = LocalDate.now().toString()
                                    if (goal.lastCheckedDate != today && goal.remaining > 0) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Complete",
                                            tint = Color.Green,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .clickable {
                                                    if (goal.remaining > 0) {
                                                        val chars = goal.dayStates.toCharArray()
                                                        val dIndex = LocalDate.now().dayOfWeek.value % 7
                                                        if (chars[dIndex] != 'C') {
                                                            chars[dIndex] = 'C'
                                                            val completed = chars.count { it == 'C' }
                                                            val updated = goal.copy(
                                                                dayStates = String(chars),
                                                                remaining = (goal.frequency - completed).coerceAtLeast(0),
                                                                lastCheckedDate = today
                                                            )
                                                            goals[index] = updated
                                                            scope.launch {
                                                                saveGoalCompletion(
                                                                    context = context,
                                                                    goalId = goal.id,
                                                                    goalHeader = goal.header,
                                                                    completionDate = LocalDate.now()
                                                                )
                                                                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                                            }
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                            val progress = (goal.frequency - goal.remaining).toFloat() / goal.frequency
                            LinearProgressIndicator(
                                progress = progress,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DayButtonsRow(states = goal.dayStates) { dayIndex ->
                                selectedGoalIndex = index
                                selectedDayIndex = dayIndex
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {
                    context.startActivity(
                        android.content.Intent(context, CalendarMenuActivity::class.java)
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = contentColorFor(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }

            FloatingActionButton(
                onClick = {
                    context.startActivity(
                        android.content.Intent(context, NotesActivity::class.java)
                    )
                },
                containerColor = MaterialTheme.colorScheme.inversePrimary,
                contentColor = contentColorFor(MaterialTheme.colorScheme.inversePrimary)
            ) {
                Icon(Icons.Default.Note, contentDescription = null)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { editingIndex = -1 },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Goal") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }

        val index = editingIndex
        if (index != null) {
            val isNew = index < 0
            val goal = if (isNew) WeeklyGoal(header = "", frequency = 1) else goals[index]
            WeeklyGoalDialog(
                initial = goal,
                onDismiss = { editingIndex = null },
                onSave = { header, freq ->
                    editingIndex = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(WeeklyGoal(header = header, frequency = freq).toEntity())
                            }
                            goals.add(WeeklyGoal(id, header, freq))
                            snackbarHostState.showSnackbar("New Weekly Activity added")
                        } else {
                            val updated = goal.copy(header = header, frequency = freq)
                            goals[index] = updated
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        }
                    }
                },
                onDelete = if (isNew) null else {
                    {
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                            withContext(Dispatchers.IO) { dao.delete(goal.toEntity()) }
                            goals.removeAt(index)
                            editingIndex = null
                        }
                    }
                },
                onProgress = if (isNew) null else {
                    {
                        val g = goals[index]
                        if (g.remaining > 0) {
                            val updated = g.copy(remaining = g.remaining - 1)
                            goals[index] = updated
                            scope.launch {
                                saveGoalCompletion(
                                    context = context,
                                    goalId = g.id,
                                    goalHeader = g.header,
                                    completionDate = LocalDate.now()
                                )
                                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        }
                    }
                }
            )
        }

        val gIndex = selectedGoalIndex
        val dIndex = selectedDayIndex
        if (gIndex != null && dIndex != null) {
            DayChoiceDialog(
                onDismiss = { selectedGoalIndex = null; selectedDayIndex = null },
                onMiss = {
                    val g = goals[gIndex]
                    val chars = g.dayStates.toCharArray()
                    val wasComplete = chars[dIndex] == 'C'
                    chars[dIndex] = 'M'
                    val completed = chars.count { it == 'C' }
                    val updated = g.copy(
                        dayStates = String(chars),
                        remaining = (g.frequency - completed).coerceAtLeast(0)
                    )
                    goals[gIndex] = updated
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }

                        if (wasComplete) {
                            val today = LocalDate.now()
                            val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
                            val date = startOfWeek.plusDays(dIndex.toLong())
                            removeGoalCompletion(context, g.id, date)
                        }
                    }
                },
                onComplete = {
                    val g = goals[gIndex]
                    val chars = g.dayStates.toCharArray()
                    val alreadyComplete = chars[dIndex] == 'C'
                    chars[dIndex] = 'C'
                    val completed = chars.count { it == 'C' }
                    val updated = g.copy(
                        dayStates = String(chars),
                        remaining = (g.frequency - completed).coerceAtLeast(0),
                        lastCheckedDate = LocalDate.now().toString()
                    )
                    goals[gIndex] = updated
                    scope.launch {
                        val today = LocalDate.now()
                        val startOfWeek = today.minusDays((today.dayOfWeek.value % 7).toLong())
                        val date = startOfWeek.plusDays(dIndex.toLong())
                        if (!alreadyComplete) {
                            saveGoalCompletion(
                                context = context,
                                goalId = g.id,
                                goalHeader = g.header,
                                completionDate = date
                            )
                        }
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        )
    }
}

@Composable
private fun WeeklyGoalDialog(
    initial: WeeklyGoal,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)? = null,
    onProgress: (() -> Unit)? = null
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
                onProgress?.let { prog ->
                    Button(
                        onClick = {
                            prog()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("Progress") }
                }
                onDelete?.let { del ->
                    TextButton(onClick = del) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
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
