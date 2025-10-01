package com.example.multi

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.util.capitalizeSentences
import com.example.multi.util.openAddressInMaps
import com.example.multi.util.showModernToast
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity that allows the user to create a new calendar event with notification scheduling.
 */
class CreateEventActivity : BaseActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showModernToast("Notification permission granted!")
        } else {
            showModernToast("Notification permission denied. Notifications may not appear.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MultiTheme(darkTheme = ThemePreferences.isDarkTheme(this)) {
                CreateEventScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Form used to capture a new event from the user with notification time selection.
 */
private fun CreateEventScreen(activity: ComponentActivity) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(activity.intent.getStringExtra(EXTRA_DATE)) }
    var showPicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState()
    var repeatOption by remember { mutableStateOf<String?>(null) }
    val dayChecks = remember { mutableStateListOf<Boolean>().apply { repeat(7) { add(false) } } }
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
    var notificationTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .height(80.dp)
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                title = {
                    Text(
                        text = "New Event",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp)
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { openAddressInMaps(context, address) }) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Open in Maps"
                    )
                }
            }
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
            Spacer(modifier = Modifier.height(16.dp))

            // Notification time picker button
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notificationTime?.let { (hour, minute) ->
                        "Notification Time: ${String.format("%02d:%02d", hour, minute)}"
                    } ?: "Set Notification Time"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && notificationTime != null) {
                        scope.launch {
                            val (hour, minute) = notificationTime!!
                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                context.startActivity(intent)
                                context.showModernToast("Please grant 'Alarms & reminders' permission to schedule exact notifications.")
                            } else {
                                val dao = EventDatabase.getInstance(context).eventDao()
                                val event = Event(
                                    title = title,
                                    description = description,
                                    date = previewDate,
                                    address = address
                                ).apply { setNotificationTime(hour, minute) }
                                val id = withContext(Dispatchers.IO) { dao.insert(event.toEntity()) }
                                val savedEvent = event.copy(id = id)
                                val success = scheduleEventNotification(
                                    context,
                                    savedEvent.id,
                                    title,
                                    description,
                                    hour,
                                    minute,
                                    previewDate
                                )
                                if (success) {
                                    context.showModernToast("Event created with notification scheduled!")
                                    activity.finish()
                                } else {
                                    withContext(Dispatchers.IO) { dao.delete(savedEvent.toEntity()) }
                                    context.showModernToast("Failed to schedule notification")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && notificationTime != null
            ) {
                Text("Create Event with Notification")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            val dao = EventDatabase.getInstance(context).eventDao()
                            val event = Event(
                                title = title,
                                description = description,
                                date = previewDate,
                                address = address
                            )
                            withContext(Dispatchers.IO) { dao.insert(event.toEntity()) }
                            context.showModernToast("Event created without notification!")
                            activity.finish()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Create Event (No Notification)")
            }
        }
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    pickerState.selectedDateMillis?.let { millis ->
                        selectedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
