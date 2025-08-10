package com.example.multi

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import com.example.multi.util.capitalizeSentences
import java.util.Calendar
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity

/**
 * Dialog used for creating a new event with optional notification and address.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    initialDate: String? = null,
    onDismiss: () -> Unit,
    onEventCreated: (Event) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()
    var notificationTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        val event = Event(
                            title = title,
                            description = description,
                            date = selectedDate,
                            address = address.ifBlank { null }
                        )
                        notificationTime?.let { (hour, minute) ->
                            val success = scheduleEventNotification(
                                context,
                                title,
                                description,
                                hour,
                                minute,
                                selectedDate
                            )
                            if (success) {
                                event.setNotificationTime(hour, minute)
                            } else {
                                Toast.makeText(context, "Failed to schedule notification", Toast.LENGTH_SHORT).show()
                            }
                        }
                        val dao = EventDatabase.getInstance(context).eventDao()
                        withContext(Dispatchers.IO) { dao.insert(event.toEntity()) }
                        onEventCreated(event)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.capitalizeSentences() },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.capitalizeSentences() },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    TextButton(onClick = { showPicker = true }) { Text("Date") }
                    selectedDate?.let { Text(it, modifier = Modifier.padding(start = 8.dp)) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        TimePickerDialog(
                            context,
                            { _, selectedHour, selectedMinute ->
                                notificationTime = Pair(selectedHour, selectedMinute)
                            },
                            hour,
                            minute,
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = notificationTime?.let { (hour, minute) ->
                            "Notification Time: ${String.format("%02d:%02d", hour, minute)}"
                        } ?: "Set Notification Time"
                    )
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

