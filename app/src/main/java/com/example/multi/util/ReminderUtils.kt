package com.example.multi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.multi.ReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun scheduleReminder(context: Context, id: Long, title: String, date: String?, time: String?) {
    if (date == null || time == null) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val localDate = LocalDate.parse(date)
    val localTime = LocalTime.parse(time)
    val trigger = LocalDateTime.of(localDate, localTime)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("id", id.toInt())
    }
    val pending = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger, pending)
    }
}

fun cancelReminder(context: Context, id: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pending = PendingIntent.getBroadcast(
        context,
        id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pending)
}
