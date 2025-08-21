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
import com.example.multi.data.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

const val EVENT_CHANNEL_ID = "event_channel"
const val EVENT_CHANNEL_NAME = "Event Reminders"
const val EVENT_CHANNEL_DESCRIPTION = "Notifications for calendar events"
const val ACTION_PROGRESS_GOAL = "action_progress_goal"
const val EXTRA_GOAL_HEADER = "extra_goal_header"

/**
 * BroadcastReceiver that displays a notification when triggered by AlarmManager.
 * Enhanced to handle event-specific notifications with better formatting and channel management.
 */
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create notification channel if it doesn't exist (required for Android 8.0+)
        createNotificationChannel(context)
        if (intent.action == ACTION_PROGRESS_GOAL) {
            val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)
            val header = intent.getStringExtra(EXTRA_GOAL_HEADER) ?: ""
            CoroutineScope(Dispatchers.Default).launch {
                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                val goals = withContext(Dispatchers.IO) { dao.getGoals() }
                val entity = goals.firstOrNull { it.id == goalId }
                entity?.let {
                    val model = it.toModel()
                    if (model.remaining > 0) {
                        val updated = model.copy(remaining = model.remaining - 1)
                        withContext(Dispatchers.IO) { dao.update(updated.toEntity()) }
                        saveGoalCompletion(
                            context = context,
                            goalId = model.id,
                            goalHeader = header.ifBlank { model.header },
                            completionDate = LocalDate.now()
                        )
                    }
                }
            }
            return
        }

        val eventType = intent.getStringExtra("event_type") ?: "general"
        if (eventType == "daily_activity") {
            CoroutineScope(Dispatchers.Default).launch {
                val dao = EventDatabase.getInstance(context).weeklyGoalDao()
                val goals = withContext(Dispatchers.IO) { dao.getGoals() }
                val target = goals.map { it.toModel() }.firstOrNull { model ->
                    val completed = model.dayStates.count { it == 'C' }
                    completed < model.frequency
                }
                if (target != null) {
                    val openIntent = Intent(context, WeeklyGoalsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val progressIntent = Intent(context, NotificationReceiver::class.java).apply {
                        action = ACTION_PROGRESS_GOAL
                        putExtra(EXTRA_GOAL_ID, target.id)
                        putExtra(EXTRA_GOAL_HEADER, target.header)
                    }
                    val progressPendingIntent = PendingIntent.getBroadcast(
                        context,
                        target.id.toInt(),
                        progressIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val notification = createGeneralNotification(
                        context,
                        "Daily Activities",
                        "You have daily activities to do.",
                        pendingIntent
                    ).addAction(
                        R.drawable.ic_launcher_foreground,
                        "Progress",
                        progressPendingIntent
                    )
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
            "event_reminder" -> {
                val openIntent = Intent(context, EventsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                createEventReminderNotification(context, title, description, pendingIntent)
            }
            else -> createGeneralNotification(context, title, description)
        }

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
    private fun createEventReminderNotification(
        context: Context,
        title: String,
        description: String,
        contentIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
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
            .setContentIntent(contentIntent)
    }

    /**
     * Creates a general notification (fallback for other notification types).
     */
    private fun createGeneralNotification(
        context: Context,
        title: String,
        description: String,
        contentIntent: PendingIntent? = null
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, EVENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
    }
}
