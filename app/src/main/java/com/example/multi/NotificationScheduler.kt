package com.example.multi

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Helper object for scheduling and displaying event notifications.
 */
object NotificationScheduler {
    const val CHANNEL_ID = "events_channel"

    /**
     * Creates the notification channel required for posting notifications on Android O+.
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Event Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules a notification for the given [event]. The event date must be in ISO format
     * (yyyy-MM-dd). If parsing fails, no notification is scheduled.
     */
    fun scheduleEventNotification(context: Context, event: Event) {
        val dateString = event.date ?: return
        val triggerTime = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val localDate = LocalDate.parse(dateString)
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                fmt.parse(dateString)?.time ?: return
            }
        } catch (e: Exception) {
            return
        }

        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra("title", event.title)
            putExtra("description", event.description)
            putExtra("id", event.id.toInt())
        }
        val pending = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
    }

    /**
     * Immediately shows a notification for debugging or fallback purposes.
     */
    fun showImmediateNotification(context: Context, event: Event) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(event.title)
            .setContentText(event.description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify(event.id.toInt(), builder.build())
        }
    }
}

