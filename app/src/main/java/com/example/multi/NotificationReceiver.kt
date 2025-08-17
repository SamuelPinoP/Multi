package com.example.multi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val EVENT_CHANNEL_ID = "event_channel"
const val EVENT_CHANNEL_NAME = "Event Reminders"
const val EVENT_CHANNEL_DESCRIPTION = "Notifications for calendar events"

/**
 * BroadcastReceiver that displays a notification when triggered by AlarmManager.
 * Enhanced to handle event-specific notifications with better formatting and channel management.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create notification channel if it doesn't exist (required for Android 8.0+)
        createNotificationChannel(context)
        val eventType = intent.getStringExtra("event_type") ?: "general"
        if (eventType == "daily_activity") {
            CoroutineScope(Dispatchers.Default).launch {
                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                val goals = withContext(Dispatchers.IO) { dao.getGoals() }
                val hasIncomplete = goals.any { entity ->
                    val model = entity.toModel()
                    val completed = model.dayStates.count { it == 'C' }
                    completed < model.frequency
                }
                if (hasIncomplete) {
                    val notification = createGeneralNotification(
                        context,
                        "Daily Activities",
                        "You have daily activities to do.",
                    )
                    val contentIntent = PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, WeeklyGoalsActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    notification.setContentIntent(contentIntent)
                    val id = System.currentTimeMillis().toInt()
                    try {
                        NotificationManagerCompat.from(context).notify(id, notification.build())
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            }
            return
        }

        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val description = intent.getStringExtra("description") ?: "You have an upcoming event."

        // Create the notification
        val notification = when (eventType) {
            "event_reminder" -> createEventReminderNotification(context, title, description)
            else -> createGeneralNotification(context, title, description)
        }
        val destination = when (eventType) {
            "event_reminder" -> EventsActivity::class.java
            else -> MainActivity::class.java
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, destination),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notification.setContentIntent(contentIntent)

        // Generate a unique notification ID based on the current time
        val notificationId = System.currentTimeMillis().toInt()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification.build())
        } catch (e: SecurityException) {
            // Handle the case where notification permission is not granted
            e.printStackTrace()
        }
    }

    /**
     * Creates a notification channel for event reminders (required for Android 8.0+).
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EVENT_CHANNEL_ID,
                EVENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = EVENT_CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates a notification specifically for event reminders.
     */
    private fun createEventReminderNotification(context: Context, title: String, description: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // You might want to use a calendar or event icon
            .setContentTitle("📅 $title")
            .setContentText(if (description.isNotBlank()) description else "Your event is coming up!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(if (description.isNotBlank()) description else "Your event '$title' is scheduled for now.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    /**
     * Creates a general notification (fallback for other notification types).
     */
    private fun createGeneralNotification(context: Context, title: String, description: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }
}

