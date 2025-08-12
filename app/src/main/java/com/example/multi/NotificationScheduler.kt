package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Schedules notifications for events. Handles one-time and weekly repeating reminders.
 * Returns true if at least one alarm was scheduled.
 */
fun scheduleEventNotification(
    context: Context,
    title: String,
    description: String,
    hour: Int,
    minute: Int,
    date: String?
): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        return false
    }

    return try {
        var scheduled = false
        val days = listOf("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
        if (!date.isNullOrBlank() && date.startsWith("Every")) {
            val intervalDays = if (date.startsWith("Every other")) 14 else 7
            days.forEachIndexed { index, day ->
                if (date.contains(day)) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, index + 1)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (timeInMillis <= System.currentTimeMillis()) {
                            add(Calendar.DAY_OF_MONTH, intervalDays)
                        }
                    }
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("title", title)
                        putExtra("description", description)
                        putExtra("event_type", "event_reminder")
                    }
                    val requestCode = (title.hashCode() + index).hashCode()
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY * intervalDays,
                        pendingIntent
                    )
                    scheduled = true
                }
            }
        } else {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
                putExtra("event_type", "event_reminder")
            }
            val requestCode = (System.currentTimeMillis() + title.hashCode()).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val calendar = Calendar.getInstance().apply {
                if (!date.isNullOrBlank()) {
                    val parts = date.split("-")
                    if (parts.size == 3) {
                        try {
                            set(Calendar.YEAR, parts[0].toInt())
                            set(Calendar.MONTH, parts[1].toInt() - 1)
                            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                        } catch (_: NumberFormatException) {}
                    }
                }
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            scheduled = true
        }
        scheduled
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Schedules a daily reminder at 11:00 AM for pending daily activities.
 */
fun scheduleDailyActivityReminder(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        return
    }

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("event_type", "daily_reminder")
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        "daily_reminder".hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 11)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}
