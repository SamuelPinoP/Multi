package com.example.multi

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.multi.ui.theme.MultiTheme
import com.example.multi.util.capitalizeSentences
import com.example.multi.ThemePreferences
import java.util.Calendar

/**
 * Activity that allows the user to create a new calendar event with notification scheduling.
 */
class CreateEventActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied. Notifications may not appear.", Toast.LENGTH_LONG).show()
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
    var notificationTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val context = LocalContext.current

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
                        val (hour, minute) = notificationTime!!
                        val success = scheduleEventNotification(activity, title, description, hour, minute)
                        if (success) {
                            Toast.makeText(context, "Event created with notification scheduled!", Toast.LENGTH_SHORT).show()
                            // Here you would typically save the event to your database
                            // and navigate back to the previous screen
                        } else {
                            Toast.makeText(context, "Failed to schedule notification", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "Event created without notification!", Toast.LENGTH_SHORT).show()
                        // Here you would save the event without notification
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Create Event (No Notification)")
            }
        }
    }
}

/**
 * Schedules a notification for the event at the specified time on the same day.
 * @param activity The ComponentActivity context to check permissions and launch settings
 * @param title The event title
 * @param description The event description
 * @param hour The hour for the notification (24-hour format)
 * @param minute The minute for the notification
 * @return true if scheduling was successful, false otherwise
 */
private fun scheduleEventNotification(activity: ComponentActivity, title: String, description: String, hour: Int, minute: Int): Boolean {
    val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Check for SCHEDULE_EXACT_ALARM permission on Android 12+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            // Direct user to settings to grant permission
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            activity.startActivity(intent)
            Toast.makeText(activity, "Please grant 'Alarms & reminders' permission to schedule exact notifications.", Toast.LENGTH_LONG).show()
            return false
        }
    }

    return try {
        val intent = Intent(activity, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("description", description)
            putExtra("event_type", "event_reminder")
        }

        // Create a unique request code based on current time and event details
        val requestCode = (System.currentTimeMillis() + title.hashCode()).toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            activity,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate the notification time for today
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Use setExactAndAllowWhileIdle for better reliability on newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        true
    } catch (e: SecurityException) {
        // This catch block handles cases where permission might still be an issue
        e.printStackTrace()
        Toast.makeText(activity, "SecurityException: Failed to schedule notification. Check permissions.", Toast.LENGTH_LONG).show()
        false
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(activity, "Error scheduling notification: ${e.message}", Toast.LENGTH_LONG).show()
        false
    }
}