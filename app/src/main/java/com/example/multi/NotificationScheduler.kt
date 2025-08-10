package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Schedules a notification for the given event details. Supports one-off and weekly recurring
 * reminders based on the provided [date] string. Dates in the format `yyyy-MM-dd` schedule a
 * single notification. Strings beginning with "Every" schedule weekly reminders on the named
 * days (e.g. "Every Monday and Wednesday").
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

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("description", description)
        putExtra("event_type", "event_reminder")
    }

    val dayMap = mapOf(
        "sunday" to Calendar.SUNDAY,
        "monday" to Calendar.MONDAY,
        "tuesday" to Calendar.TUESDAY,
        "wednesday" to Calendar.WEDNESDAY,
        "thursday" to Calendar.THURSDAY,
        "friday" to Calendar.FRIDAY,
        "saturday" to Calendar.SATURDAY,
    )

    val lower = date?.lowercase().orEmpty()
    val dayRegex = "(sunday|monday|tuesday|wednesday|thursday|friday|saturday)".toRegex()
    val days = dayRegex.findAll(lower).mapNotNull { dayMap[it.value] }.toList()
    val interval = when {
        lower.startsWith("every other") -> AlarmManager.INTERVAL_DAY * 14
        lower.startsWith("every") -> AlarmManager.INTERVAL_DAY * 7
        else -> null
    }

    return try {
        if (days.isNotEmpty() && interval != null) {
            days.forEach { day ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, day)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }
                val requestCode = (System.currentTimeMillis() + title.hashCode() + day).toInt()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    interval,
                    pendingIntent
                )
            }
            true
        } else {
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
                        } catch (_: NumberFormatException) {
                        }
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
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            true
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        false
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

