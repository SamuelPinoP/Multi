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

private const val CHANNEL_ID = "event_reminders"

fun scheduleReminder(context: Context, event: Event) {
    val date = event.date ?: return
    val parts = event.reminderTime.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val triggerAt = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate = java.time.LocalDate.parse(date)
            val time = java.time.LocalTime.of(hour, minute)
            java.time.ZonedDateTime.of(localDate, time, java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            fmt.parse("$date $hour:$minute")!!.time
        }
    }.getOrNull() ?: return

    createChannel(context)
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", event.title)
        putExtra("description", event.description)
        putExtra("id", event.id)
    }
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
}

fun cancelReminder(context: Context, event: Event) {
    val intent = Intent(context, ReminderReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pending)
}

private fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(CHANNEL_ID, "Event Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

class ReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        createChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(intent.getStringExtra("title"))
            .setContentText(intent.getStringExtra("description"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(intent.getLongExtra("id", 0L).toInt(), builder.build())
        }
    }
}
