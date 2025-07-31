package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {
    fun schedule(context: Context, event: Event) {
        if (!event.reminderEnabled || event.date == null) return
        try {
            val date = LocalDate.parse(event.date)
            val time = LocalTime.parse(event.reminderTime)
            val trigger = LocalDateTime.of(date, time)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("eventId", event.id)
                putExtra("title", event.title)
                putExtra("desc", event.description)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                event.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
        } catch (_: Exception) {
            // Invalid date/time
        }
    }

    fun cancel(context: Context, eventId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending)
    }
}
