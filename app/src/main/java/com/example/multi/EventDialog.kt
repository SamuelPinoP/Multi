package com.example.multi

import android.app.TimePickerDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.multi.util.capitalizeSentences
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/** Dialog used for editing existing events without notification support. */
fun EventDialog(
    initial: Event,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?, Pair<Int, Int>?, Boolean) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var title by remember { mutableStateOf(initial.title) }
    var description by remember { mutableStateOf(initial.description) }
    var address by remember { mutableStateOf(initial.address ?: "") }
    var selectedDate by remember { mutableStateOf(initial.date) }
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()
    var repeatOption by remember { mutableStateOf<String?>(null) }
    val dayChecks = remember {
        mutableStateListOf<Boolean>().apply { repeat(7) { add(false) } }
    }
    val repeatDescription by remember {
        derivedStateOf { EventDateUtils.buildRepeatDescription(repeatOption, dayChecks.toList()) }
    }
    val nextOccurrence by remember {
        derivedStateOf { EventDateUtils.computeNextOccurrence(dayChecks.toList()) }
    }
    val previewDate by remember {
        derivedStateOf {
            EventDateUtils.formatPreview(
                selectedDate,
                repeatDescription,
                if (repeatDescription != null) nextOccurrence else null
            )
        }
    }
    val storedDate by remember {
        derivedStateOf {
            EventDateUtils.buildStoredDate(
                repeatDescription,
                if (repeatDescription != null) nextOccurrence else null,
                selectedDate
            )
        }
    }
    var notificationTime by remember {
        mutableStateOf(
            if (initial.notificationEnabled &&
                initial.notificationHour != null &&
                initial.notificationMinute != null
            ) {
                Pair(initial.notificationHour!!, initial.notificationMinute!!)
            } else {
                null
            }
        )
    }
    var notificationEnabled by remember { mutableStateOf(notificationTime != null) }
    val context = LocalContext.current

    fun launchTimePicker() {
        val calendar = Calendar.getInstance()
        val initialHour = notificationTime?.first ?: calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = notificationTime?.second ?: calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                notificationTime = Pair(hour, minute)
                notificationEnabled = true
            },
            initialHour,
            initialMinute,
            true
        )
        dialog.setOnDismissListener {
            if (notificationEnabled && notificationTime == null) {
                notificationEnabled = false
            }
        }
        dialog.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        description,
                        storedDate,
                        address.ifBlank { null },
                        notificationTime,
                        notificationEnabled && notificationTime != null
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
                                onCheckedChange = {
                                    dayChecks[i] = it
                                    val computedDate = EventDateUtils.computeNextOccurrence(dayChecks.toList())
                                    if (!computedDate.isNullOrBlank()) {
                                        selectedDate = computedDate
                                    }
                                }
                            )
                            Text(letters[i])
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconToggleButton(
                        checked = notificationEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                notificationEnabled = true
                                if (notificationTime == null) {
                                    launchTimePicker()
                                }
                            } else {
                                notificationEnabled = false
                                notificationTime = null
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (notificationEnabled) {
                                Icons.Filled.Notifications
                            } else {
                                Icons.Outlined.NotificationsNone
                            },
                            contentDescription = if (notificationEnabled) {
                                "Notification enabled"
                            } else {
                                "Notification disabled"
                            }
                        )
                    }
                    if (notificationEnabled) {
                        TextButton(onClick = { launchTimePicker() }) {
                            Text(
                                text = notificationTime?.let { (hour, minute) ->
                                    "Notification Time: ${String.format("%02d:%02d", hour, minute)}"
                                } ?: "Set Notification Time"
                            )
                        }
                    } else {
                        Text(
                            text = "Notification off",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
