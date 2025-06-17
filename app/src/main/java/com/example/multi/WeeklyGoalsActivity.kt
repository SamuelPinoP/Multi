package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class WeeklyGoalsActivity : SegmentActivity("Weekly Goals") {
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
        val stored = withContext(Dispatchers.IO) { dao.getGoals() }
        val week = currentWeek()
        val list = stored.map { entity ->
            var model = entity.toModel()
            if (model.weekNumber != week) {
                model = model.copy(remaining = model.frequency, weekNumber = week, lastCheckedDate = null)
                withContext(Dispatchers.IO) { dao.update(model.toEntity()) }
            }
            model
        }
        goals.clear()
        goals.addAll(list)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* TODO: Historial action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .padding(end = 8.dp)
                ) {
                    androidx.compose.material.Text("Historial", color = Color.White, fontSize = 20.sp)
                }
                Button(
                    onClick = { editingIndex = -1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8)),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .padding(start = 8.dp)
                ) {
                    androidx.compose.material.Text("Edit", color = Color.White, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val remaining = daysRemainingInWeek()
            androidx.compose.material.Text(
                text = "$remaining days remaining",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material.Text(
                text = "Today's Goals",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(goals) { index, goal ->
                    Card(
                        elevation = CardDefaults.cardElevation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingIndex = index }
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            if (goal.remaining == 0) {
                                androidx.compose.material.Text(
                                    text = "Completed",
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
                                androidx.compose.material.Text(
                                    text = goal.header,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material.Text(
                                        text = "${goal.remaining}/${goal.frequency}",
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
                        }
                    }
                }
            }
        }

        val index = editingIndex
        if (index != null) {
            val isNew = index < 0
            val goal = if (isNew) WeeklyGoal("", 1) else goals[index]
            WeeklyGoalDialog(
                initial = goal,
                onDismiss = { editingIndex = null },
                onSave = { header, freq ->
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(WeeklyGoal(header = header, frequency = freq).toEntity())
                            }
                            goals.add(WeeklyGoal(id, header, freq))
                            snackbarHostState.showSnackbar("New Weekly Activity added")
                        } else {
                            val updated = WeeklyGoal(goal.id, header, freq)
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            goals[index] = updated
                        }
                        editingIndex = null
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
            ) { androidx.compose.material.Text("Save") }
        },
        dismissButton = {
            Row {
                onProgress?.let { prog ->
                    Button(
                        onClick = {
                            prog()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) { androidx.compose.material.Text("Progress") }
                }
                onDelete?.let { del ->
                    TextButton(onClick = del) { androidx.compose.material.Text("Delete") }
                }
                TextButton(onClick = onDismiss) { androidx.compose.material.Text("Cancel") }
            }
        },
        title = { androidx.compose.material.Text("Custom your Weekly Routine!") },
        text = {
            Column {
                androidx.compose.material.Text("Header", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material.Text("Frequency", style = MaterialTheme.typography.bodySmall)
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
                            androidx.compose.material.Text(text)
                        }
                    }
                }
            }
        }
    )
}
