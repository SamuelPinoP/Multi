package com.example.multi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.multi.Event
import com.example.multi.EventNotificationReceiver

/** Schedules a notification for the given event at the specified time. */
fun scheduleEventNotification(context: Context, event: Event, triggerAtMillis: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EventNotificationReceiver::class.java).apply {
        putExtra("title", event.title)
        putExtra("description", event.description)
        putExtra("notification_id", event.id.toInt())
    }
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
}
