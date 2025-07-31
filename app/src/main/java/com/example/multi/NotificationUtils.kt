package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun scheduleEventNotification(context: Context, event: Event) {
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EventNotificationReceiver::class.java).apply {
        putExtra("id", event.id.toInt())
        putExtra("title", event.title)
        putExtra("desc", event.description)
    }
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    manager.cancel(pending)
    val date = event.date ?: return
    val time = event.notifyTime ?: return
    try {
        val localDate = LocalDate.parse(date)
        val localTime = LocalTime.parse(time)
        val trigger = localDate.atTime(localTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
    } catch (_: Exception) {
        // ignore parse errors
    }
}

fun cancelEventNotification(context: Context, event: Event) {
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EventNotificationReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    manager.cancel(pending)
}
