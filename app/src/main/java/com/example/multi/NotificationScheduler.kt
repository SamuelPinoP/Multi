package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

const val DAILY_ACTIVITY_REQUEST_CODE = 10001

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
    date: String?,
    firstTriggerOverride: String? = null
): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        return false
    }

    return try {
        var scheduled = false
        val parsedDate = EventDateUtils.parseStoredDate(date)
        val repeatDescription = parsedDate.repeatDescription
        val explicitDate = parsedDate.explicitDate
        val storedNextDate = parsedDate.nextOccurrence
        val days = EventDateUtils.dayNames()
        if (!repeatDescription.isNullOrBlank() && repeatDescription.startsWith("Every")) {
            val intervalDays = if (repeatDescription.startsWith("Every other", ignoreCase = true)) 14 else 7
            val selectedDays = days.mapIndexedNotNull { index, day ->
                if (repeatDescription.contains(day, ignoreCase = true)) index else null
            }
            val overrideCalendar = EventDateUtils.parseDateToCalendar(firstTriggerOverride ?: storedNextDate)
            selectedDays.forEach { index ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    val matchedOverride = overrideCalendar?.takeIf {
                        it.get(Calendar.DAY_OF_WEEK) == index + 1
                    }
                    if (matchedOverride != null) {
                        set(Calendar.YEAR, matchedOverride.get(Calendar.YEAR))
                        set(Calendar.MONTH, matchedOverride.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, matchedOverride.get(Calendar.DAY_OF_MONTH))
                    } else {
                        set(Calendar.DAY_OF_WEEK, index + 1)
                    }
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
                val targetDate = firstTriggerOverride ?: storedNextDate ?: explicitDate ?: date
                if (!targetDate.isNullOrBlank()) {
                    EventDateUtils.parseDateToCalendar(targetDate)?.let { parsed ->
                        set(Calendar.YEAR, parsed.get(Calendar.YEAR))
                        set(Calendar.MONTH, parsed.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, parsed.get(Calendar.DAY_OF_MONTH))
                    } ?: run {
                        val parts = targetDate.split("-")
                        if (parts.size == 3) {
                            try {
                                set(Calendar.YEAR, parts[0].toInt())
                                set(Calendar.MONTH, parts[1].toInt() - 1)
                                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                            } catch (_: NumberFormatException) {}
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
 * Schedules a daily alarm at midnight that triggers a notification reminding
 * the user about incomplete daily activities.
 */
fun scheduleDailyActivityReminder(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        return
    }

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("event_type", "daily_activity")
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        DAILY_ACTIVITY_REQUEST_CODE,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
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
