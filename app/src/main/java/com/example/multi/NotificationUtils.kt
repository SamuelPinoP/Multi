package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Locale

private fun parseMillis(date: String, time: String): Long? {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        fmt.parse("$date $time")?.time
    } catch (_: Exception) {
        null
    }
}

fun scheduleEventNotification(context: Context, event: Event) {
    if (!event.reminderEnabled || event.date == null) return
    val millis = parseMillis(event.date!!, event.reminderTime) ?: return
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", event.title)
        putExtra("text", event.description)
    }
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.setExact(AlarmManager.RTC_WAKEUP, millis, pending)
}

fun cancelEventNotification(context: Context, id: Long) {
    val intent = Intent(context, NotificationReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    am.cancel(pending)
}
