package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun scheduleReminder(context: Context, event: Event) {
    if (!event.reminderEnabled || event.date == null || event.reminderTime == null) return
    val millis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val dt = LocalDateTime.parse(
            "${event.date} ${event.reminderTime}",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )
        dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } else {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        fmt.parse("${event.date} ${event.reminderTime}")!!.time
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("id", event.id)
        putExtra("title", event.title)
    }
    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
}

fun cancelReminder(context: Context, id: Long) {
    val intent = Intent(context, ReminderReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pending)
}
