package com.example.multi

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import com.example.multi.util.capitalizeSentences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    initial: Event,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?, Boolean, String?) -> Unit,
    onDelete: (() -> Unit)? = null,
    isNew: Boolean = false,
) {
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }
    var address by remember { mutableStateOf(initial.address ?: "") }
    var selectedDate by remember { mutableStateOf(initial.date) }
    var showPicker by remember { mutableStateOf(false) }
    var reminderEnabled by remember { mutableStateOf(initial.reminderEnabled) }
    var reminderTime by remember { mutableStateOf(initial.reminderTime ?: "11:00") }
    var showTimePicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()
    var repeatOption by remember { mutableStateOf<String?>(null) }
    val dayChecks = remember {
        mutableStateListOf<Boolean>().apply { repeat(7) { add(false) } }
    }
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
                val prefix = when (repeatOption) {
                    "Every" -> "Every"
                    "Every other" -> "Every other"
                    else -> ""
                }
                val dayString = when (selectedNames.size) {
                    1 -> selectedNames.first()
                    2 -> "${selectedNames[0]} and ${selectedNames[1]}"
                    else -> selectedNames.dropLast(1).joinToString(", ") + " and " + selectedNames.last()
                }
                if (prefix.isNotEmpty()) "$prefix $dayString" else dayString
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
                        val prefix = when (repeatOption) {
                            "Every" -> "Every"
                            "Every other" -> "Every other"
                            else -> ""
                        }
                        val dayString = when (selectedNames.size) {
                            1 -> selectedNames.first()
                            2 -> "${selectedNames[0]} and ${selectedNames[1]}"
                            else -> selectedNames.dropLast(1).joinToString(", ") + " and " + selectedNames.last()
                        }
                        if (prefix.isNotEmpty()) "$prefix $dayString" else dayString
                    } else {
                        selectedDate
                    }
                    onSave(
                        title,
                        description,
                        finalDate,
                        address.ifBlank { null },
                        reminderEnabled,
                        reminderTime
                    )
                },
                enabled = title.isNotBlank(),
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
                    onValueChange = { title = it.capitalizeSentences() },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.capitalizeSentences() },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showPicker = true }) { Text("Date") }
                    previewDate?.let { Text(it, modifier = Modifier.padding(start = 8.dp)) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reminder", modifier = Modifier.weight(1f))
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                    IconButton(onClick = { showTimePicker = true }, enabled = reminderEnabled) {
                        Icon(
                            imageVector = if (reminderEnabled) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (reminderEnabled) {
                        Text(reminderTime, modifier = Modifier.padding(start = 4.dp))
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

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        ) {
            val parts = reminderTime.split(":").map { it.toInt() }
            val timeState = rememberTimePickerState(
                initialHour = parts.getOrNull(0) ?: 11,
                initialMinute = parts.getOrNull(1) ?: 0,
                is24Hour = false
            )
            TimePicker(state = timeState)
            LaunchedEffect(timeState.hour, timeState.minute) {
                reminderTime = "%02d:%02d".format(timeState.hour, timeState.minute)
            }
        }
    }
}
