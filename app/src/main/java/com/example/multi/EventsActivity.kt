package com.example.multi

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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

/** Activity displaying the list of user events. */
class EventsActivity : SegmentActivity("Events") {
    @Composable
    override fun SegmentContent() {
        EventsScreen()
    }
}

@Composable
private fun EventsScreen() {
    val context = LocalContext.current
    val events = remember { mutableStateListOf<Event>() }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        events.clear()
        events.addAll(stored.map { it.toModel() })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(events) { index, event ->
                ElevatedCard(
                    elevation = CardDefaults.elevatedCardElevation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingIndex = index }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${index + 1}. ${event.title}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (event.description.isNotBlank()) {
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        event.date?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
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
            androidx.compose.foundation.text.ClickableText(
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

        ExtendedFloatingActionButton(
            onClick = { editingIndex = -1 },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Event") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 68.dp, end = 16.dp)
        )

        val index = editingIndex
        if (index != null) {
            val isNew = index < 0
            val event = if (isNew) Event(0L, "", "", null) else events[index]
            EventDialog(
                initial = event,
                onDismiss = { editingIndex = null },
                onSave = { title, desc, date ->
                    editingIndex = null
                    scope.launch {
                        val dao = EventDatabase.getInstance(context).eventDao()
                        if (isNew) {
                            val id = withContext(Dispatchers.IO) {
                                dao.insert(Event(title = title, description = desc, date = date).toEntity())
                            }
                            events.add(Event(id, title, desc, date))
                            snackbarHostState.showSnackbar("New Event added")
                        } else {
                            val updated = Event(event.id, title, desc, date)
                            withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                            events[index] = updated
                        }
                    }
                },
                onDelete = if (isNew) null else {
                    {
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).eventDao()
                            withContext(Dispatchers.IO) { dao.delete(event.toEntity()) }
                            events.removeAt(index)
                            editingIndex = null
                        }
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp)
        )
    }
}

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
            Button(
                onClick = {
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
                            else -> selectedNames.dropLast(1).joinToString(", ") + " and " + selectedNames.last()
                        }
                        "$prefix $dayString"
                    } else {
                        selectedDate
                    }
                    onSave(title, description, finalDate)
                },
                enabled = title.isNotBlank()
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
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
