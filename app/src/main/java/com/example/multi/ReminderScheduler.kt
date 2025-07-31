package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

object ReminderScheduler {
    fun schedule(context: Context, event: Event) {
        if (!event.reminderEnabled) return
        val dateStr = event.date ?: return
        val timeStr = event.reminderTime ?: return
        val date = try {
            LocalDate.parse(dateStr)
        } catch (_: DateTimeParseException) {
            return
        }
        val parts = timeStr.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        val millis = date.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            putExtra(EventReminderReceiver.EXTRA_TITLE, event.title)
            putExtra(EventReminderReceiver.EXTRA_ID, event.id)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
        } else {
            mgr.setExact(AlarmManager.RTC_WAKEUP, millis, pending)
        }
    }

    fun cancel(context: Context, eventId: Long) {
        val intent = Intent(context, EventReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.cancel(pending)
    }
}
