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
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

const val EVENT_CHANNEL_ID = "event_channel"
const val EVENT_CHANNEL_NAME = "Event Reminders"
const val EVENT_CHANNEL_DESCRIPTION = "Notifications for calendar events"
const val DAILY_CHANNEL_ID = "daily_channel"
const val DAILY_CHANNEL_NAME = "Daily Activity Reminders"
const val DAILY_CHANNEL_DESCRIPTION = "Notifications for pending daily activities"

/**
 * BroadcastReceiver that displays a notification when triggered by AlarmManager.
 * Enhanced to handle event-specific notifications with better formatting and channel management.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val description = intent.getStringExtra("description") ?: "You have an upcoming event."
        val eventType = intent.getStringExtra("event_type") ?: "general"

        // Create notification channel if it doesn't exist (required for Android 8.0+)
        createNotificationChannel(context, eventType)

        // Create the notification based on type
        val builder = when (eventType) {
            "event_reminder" -> createEventReminderNotification(context, title, description)
            "daily_goal" -> createDailyGoalsNotification(context)
            else -> createGeneralNotification(context, title, description)
        }

        if (builder != null) {
            val notificationId = System.currentTimeMillis().toInt()
            try {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Handle the case where notification permission is not granted
                e.printStackTrace()
            }
        }
    }

    /**
     * Creates a notification channel for event reminders (required for Android 8.0+).
     */
    private fun createNotificationChannel(context: Context, eventType: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (id, name, description) = when (eventType) {
                "daily_goal" -> Triple(DAILY_CHANNEL_ID, DAILY_CHANNEL_NAME, DAILY_CHANNEL_DESCRIPTION)
                else -> Triple(EVENT_CHANNEL_ID, EVENT_CHANNEL_NAME, EVENT_CHANNEL_DESCRIPTION)
            }
            val channel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                this.description = description
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

    /**
     * Creates a notification listing pending daily activities.
     * Returns null if no activities are pending today.
     */
    private fun createDailyGoalsNotification(context: Context): NotificationCompat.Builder? {
        val dao = EventDatabase.getInstance(context).weeklyGoalDao()
        val goals = runBlocking { dao.getGoals() }
        val todayIndex = LocalDate.now().dayOfWeek.value % 7
        val pending = goals.filter { it.remaining > 0 && it.dayStates.getOrNull(todayIndex) != 'C' }
            .map { it.header }

        if (pending.isEmpty()) return null

        val content = pending.joinToString(", ")
        return NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pending Daily Activities")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }
}
