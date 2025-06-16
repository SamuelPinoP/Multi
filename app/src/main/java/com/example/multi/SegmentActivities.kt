package com.example.multi

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import kotlin.collections.buildList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.Text
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import com.example.multi.ui.theme.MultiTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * Base activity used for each feature segment of the application.
 *
 * It provides a standard layout with a title bar and hosts the composable
 * returned from [SegmentContent].
 */
open class SegmentActivity(private val segmentTitle: String) : ComponentActivity() {
    /** Content displayed inside the [SegmentScreen]. */
    @Composable
    open fun SegmentContent() {
        Text(segmentTitle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MultiTheme {
                SegmentScreen(
                    title = segmentTitle,
                    onBack = { finish() },
                    onClose = { finishAffinity() }
                ) {
                    SegmentContent()
                }
            }
        }
    }
}

/** Activity hosting the [CalendarView]. */
class CalendarActivity : SegmentActivity("Calendar") {
    @Composable
    override fun SegmentContent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CalendarView()
        } else {
            Text("Calendar requires Android O or higher")
        }
    }
}
/** Activity displaying the list of user events. */
class EventsActivity : SegmentActivity("Events") {
    @Composable
    override fun SegmentContent() {
        EventsScreen()
    }
}

/** Model representing a single event entry with an optional date. */
data class Event(
    var title: String,
    var description: String,
    var date: String? = null
)


/**
 * Screen that displays a list of events and allows creating or editing them.
 */
@Composable
private fun EventsScreen() {
    val listSaver = listSaver<MutableList<Event>, String>(
        save = { list ->
            buildList<String> {
                list.forEach { event ->
                    add(event.title)
                    add(event.description)
                    add(event.date ?: "")
                }
            }
        },
        restore = { items ->
            items.chunked(3).map { Event(it[0], it[1], it[2].ifBlank { null }) }
                .toMutableStateList()
        }
    )
    val events = rememberSaveable(saver = listSaver) { mutableStateListOf<Event>() }
    var editingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(events) { index, event ->
                Card(
                    elevation = CardDefaults.cardElevation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingIndex = index }
                ) {
                    Text(
                        text = buildString {
                            append("${index + 1} - ${event.title}")
                            event.date?.let { append("  Date: $it") }
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (events.isEmpty()) {
            val annotated = buildAnnotatedString {
                append("No events, ")
                pushStringAnnotation(tag = "ADD", annotation = "add")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append("add")
                }
                pop()
                append(" some!")
            }
            ClickableText(
                text = annotated,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    fontSize = 18.sp
                ),
                modifier = Modifier.align(Alignment.Center),
                onClick = { offset ->
                    annotated.getStringAnnotations("ADD", offset, offset)
                        .firstOrNull()?.let { editingIndex = -1 }
                }
            )
        }

        FloatingActionButton(
            onClick = { editingIndex = -1 },
            backgroundColor = Color(0xFF4CAF50),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 68.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }

        val index = editingIndex
        if (index != null) {
            val isNew = index < 0
            val event = if (isNew) Event("", "", null) else events[index]
            EventDialog(
                initial = event,
                onDismiss = { editingIndex = null },
                onSave = { title, desc, date ->
                    if (isNew) {
                        events.add(Event(title, desc, date))
                        scope.launch {
                            snackbarHostState.showSnackbar("New Event added")
                        }
                    } else {
                        events[index] = Event(title, desc, date)
                    }
                    editingIndex = null
                },
                onDelete = if (isNew) null else {
                    {
                        events.removeAt(index)
                        editingIndex = null
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp) // move it 64dp higher, adjust as needed
        )
    }
}

/**
 * Dialog used for creating or editing an [Event].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDialog(
    initial: Event,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }
    var selectedDate by remember { mutableStateOf(initial.date) }
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()
    var repeatOption by remember { mutableStateOf("Every") }
    val dayChecks = remember { mutableStateListOf(false, false, false, false, false, false, false) }
    val previewDate by remember {
        derivedStateOf {
            val daysFull = listOf(
                "Sunday",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday"
            )
            val selectedNames = daysFull.filterIndexed { index, _ -> dayChecks[index] }
            if (selectedNames.isNotEmpty()) {
                val prefix = if (repeatOption == "Every") "Every" else "Every other"
                val dayString = when (selectedNames.size) {
                    1 -> selectedNames.first()
                    2 -> "${selectedNames[0]} and ${selectedNames[1]}"
                    else -> selectedNames.dropLast(1).joinToString(", ") + " and " + selectedNames.last()
                }
                "$prefix $dayString"
            } else {
                selectedDate
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val daysFull = listOf(
                    "Sunday",
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday"
                )
                val selectedNames = daysFull.filterIndexed { index, _ -> dayChecks[index] }
                val finalDate = if (selectedNames.isNotEmpty()) {
                    val prefix = if (repeatOption == "Every") "Every" else "Every other"
                    val dayString = when (selectedNames.size) {
                        1 -> selectedNames.first()
                        2 -> "${selectedNames[0]} and ${selectedNames[1]}"
                        else ->
                            selectedNames.dropLast(1).joinToString(", ") + " and " + selectedNames.last()
                    }
                    "$prefix $dayString"
                } else {
                    selectedDate
                }
                onSave(title, description, finalDate)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showPicker = true }) { Text("Date") }
                    previewDate?.let { Text(it, modifier = Modifier.padding(start = 8.dp)) }
                    Spacer(modifier = Modifier.weight(1f))
                    onDelete?.let { del ->
                        TextButton(onClick = del) { Text("Delete") }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val selectedColor = MaterialTheme.colorScheme.primary
                    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant
                    Button(
                        onClick = { repeatOption = "Every" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (repeatOption == "Every") selectedColor else unselectedColor
                        )
                    ) { Text("Every") }
                    Button(
                        onClick = { repeatOption = "Every other" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (repeatOption == "Every other") selectedColor else unselectedColor
                        )
                    ) { Text("Every Other") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val letters = listOf("S", "M", "T", "W", "T", "F", "S")
                    for (i in 0..6) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Checkbox(
                                checked = dayChecks[i],
                                onCheckedChange = { dayChecks[i] = it }
                            )
                            Text(letters[i])
                        }
                    }
                }
            }
        }
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    pickerState.selectedDateMillis?.let { millis ->
                        selectedDate = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                        } else {
                            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            fmt.format(java.util.Date(millis))
                        }
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
class WorkoutActivity : SegmentActivity("Workout") {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Today's workout", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\u2022 Push-ups\n\u2022 Sit-ups\n\u2022 Squats")
        }
    }
}

class NotesActivity : SegmentActivity("Notes") {
    @Composable
    override fun SegmentContent() {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("This is where your notes will appear.")
        }
    }
}

/** Activity presenting the weekly goals feature. */
class WeeklyGoalsActivity : SegmentActivity("Weekly Goals") {
    @Composable
    override fun SegmentContent() {
        WeeklyGoalsScreen()
    }
}

/** Model representing a user's recurring weekly goal. */
data class WeeklyGoal(var header: String, var frequency: Int)

/** Returns the number of days left before this week ends. */
private fun daysRemainingInWeek(): Int {
    val today = LocalDate.now()
    val nextSunday = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
    return ChronoUnit.DAYS.between(today, nextSunday).toInt() - 1
}

/**
 * Screen that manages and displays the user's weekly goals.
 */
@Composable
private fun WeeklyGoalsScreen() {
    val listSaver = listSaver<MutableList<WeeklyGoal>, String>(
        save = { list ->
            buildList {
                list.forEach { goal ->
                    add(goal.header)
                    add(goal.frequency.toString())
                }
            }
        },
        restore = { items ->
            items.chunked(2).map { WeeklyGoal(it[0], it[1].toInt()) }.toMutableStateList()
        }
    )

    val goals = rememberSaveable(saver = listSaver) { mutableStateListOf<WeeklyGoal>() }
    var editingIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                    Text("Historial", color = Color.White, fontSize = 20.sp)
                }
                Button(
                    onClick = { editingIndex = -1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8)),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .padding(start = 8.dp)
                ) {
                    Text("Edit", color = Color.White, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.header,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${goal.frequency}/7",
                                style = MaterialTheme.typography.bodyLarge
                            )
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
                    if (isNew) {
                        goals.add(WeeklyGoal(header, freq))
                        scope.launch {
                            snackbarHostState.showSnackbar("New Weekly Activity added")
                        }
                    } else {
                        goals[index] = WeeklyGoal(header, freq)
                    }
                    editingIndex = null
                },
                onDelete = if (isNew) null else {
                    {
                        goals.removeAt(index)
                        editingIndex = null
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp) // move it 64dp higher, adjust as needed
        )
    }
}

/**
 * Dialog used for creating or editing a [WeeklyGoal].
 */
@Composable
private fun WeeklyGoalDialog(
    initial: WeeklyGoal,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
    onDelete: (() -> Unit)? = null
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
        title = { Text("Custom your Weekly Routine!") },
        text = {
            Column {
                Text("Header", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it },
                    modifier = Modifier.fillMaxWidth()
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

/**
 * Common scaffold used by the segment activities for consistent layout.
 */
@Composable
fun SegmentScreen(
    title: String,
    onBack: () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.height(135.dp),
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
