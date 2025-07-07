package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.example.multi.util.capitalizeSentences
import androidx.compose.ui.res.stringResource

class WeeklyGoalsActivity : SegmentActivity(R.string.label_weekly_goals) {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        WeeklyGoalsScreen()
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeeklyGoalsScreen() {
    val context = LocalContext.current
    val goals = remember { mutableStateListOf<WeeklyGoal>() }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
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
                val completed = model.frequency - model.remaining
                val record = WeeklyGoalRecord(
                    header = model.header,
                    completed = completed,
                    frequency = model.frequency,
                    weekStart = prevStartStr,
                    weekEnd = prevEndStr
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
            goals.add(model)
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
                    Text(stringResource(R.string.record), fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val remaining = daysRemainingInWeek()
            Text(
                text = stringResource(R.string.days_remaining, remaining),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.todays_goals),
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
                                    text = stringResource(R.string.completed),
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
                                            contentDescription = stringResource(R.string.complete),
                                            tint = Color.Green,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .clickable {
                                                    if (goal.remaining > 0) {
                                                        val updated = goal.copy(
                                                            remaining = goal.remaining - 1,
                                                            lastCheckedDate = today
                                                        )
                                                        goals[index] = updated
                                                        scope.launch {
                                                            val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
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
                                val todayIndex = LocalDate.now().dayOfWeek.value % 7
                                if (dayIndex == todayIndex && goal.dayStates[dayIndex] == '-') {
                                    val chars = goal.dayStates.toCharArray()
                                    chars[dayIndex] = 'C'
                                    val updated = goal.copy(
                                        dayStates = String(chars),
                                        lastCheckedDate = LocalDate.now().toString(),
                                        remaining = (goal.remaining - 1).coerceAtLeast(0)
                                    )
                                    goals[index] = updated
                                    scope.launch {
                                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { editingIndex = -1 },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.add_goal)) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        )

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
                            snackbarHostState.showSnackbar(stringResource(R.string.new_weekly_activity_added))
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
                                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                                withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            }
                        }
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
            ) { Text(stringResource(R.string.save)) }
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
                    ) { Text(stringResource(R.string.progress)) }
                }
                onDelete?.let { del ->
                    TextButton(onClick = del) { Text(stringResource(R.string.delete)) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
        title = { Text(stringResource(R.string.customize_weekly_routine)) },
        text = {
            Column {
                Text(stringResource(R.string.header), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it.capitalizeSentences() },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.frequency), style = MaterialTheme.typography.bodySmall)
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

@Composable
private fun DayButtonsRow(states: String, onClick: (Int) -> Unit) {
    val labels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        labels.forEachIndexed { index, label ->
            val color = when (states[index]) {
                'C' -> Color(0xFF4CAF50)
                'M' -> Color.Red
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Button(
                onClick = { onClick(index) },
                colors = ButtonDefaults.buttonColors(containerColor = color),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Text(label)
            }
        }
    }
}
