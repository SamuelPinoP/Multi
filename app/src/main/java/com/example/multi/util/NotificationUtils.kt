package com.example.multi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.multi.Event
import com.example.multi.ReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun scheduleEventReminder(context: Context, event: Event) {
    if (!event.reminderEnabled) return
    val dateStr = event.date ?: return
    val timeStr = event.reminderTime ?: return
    try {
        val date = LocalDate.parse(dateStr)
        val time = LocalTime.parse(timeStr)
        val dateTime = LocalDateTime.of(date, time)
        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = ReminderReceiver.pendingIntent(context, event)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
    } catch (_: Exception) {
        // ignore parse errors
    }
}

fun cancelEventReminder(context: Context, eventId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        eventId.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pending)
}
