package com.example.multi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate

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

        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val description = intent.getStringExtra("description") ?: "You have an upcoming event."
        val eventType = intent.getStringExtra("event_type") ?: "general"
        val notification = when (eventType) {
            "event_reminder" -> createEventReminderNotification(context, title, description)
            "daily_pending" -> {
                val headers = runBlocking {
                    val db = EventDatabase.getInstance(context)
                    val goals = withContext(Dispatchers.IO) { db.weeklyGoalDao().getGoals() }
                    val today = LocalDate.now().toString()
                    val completed = withContext(Dispatchers.IO) { db.dailyCompletionDao().getCompletionsForDate(today) }
                    val completedIds = completed.map { it.goalId }.toSet()
                    goals.filter { it.id !in completedIds }.map { it.header }
                }
                if (headers.isNotEmpty()) createDailyPendingNotification(context, headers) else null
            }
            else -> createGeneralNotification(context, title, description)
        }

        notification?.let {
            val notificationId = System.currentTimeMillis().toInt()
            try {
                NotificationManagerCompat.from(context).notify(notificationId, it.build())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
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
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(if (description.isNotBlank()) description else "Your event '$title' is scheduled for now."))
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

    private fun createDailyPendingNotification(context: Context, headers: List<String>): NotificationCompat.Builder {
        val summary = if (headers.size <= 3) headers.joinToString(", ") else headers.take(3).joinToString(", ") + "..."
        return NotificationCompat.Builder(context, EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Activities")
            .setContentText("Pending: $summary")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Pending: ${headers.joinToString(", ")}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }
}
