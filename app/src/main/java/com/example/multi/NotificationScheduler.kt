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
    event: Event
): Boolean {
    if (event.id == 0L || !event.hasValidNotificationTime()) {
        return false
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        return false
    }

    return try {
        cancelEventNotification(context, event)

        var scheduled = false
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", event.title)
            putExtra("description", event.description)
            putExtra("event_type", "event_reminder")
        }

        val repeatDays = extractRepeatingDayIndices(event.date)
        if (repeatDays.isNotEmpty()) {
            val intervalDays = if (event.date?.startsWith("Every other") == true) 14 else 7
            repeatDays.forEach { dayIndex ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, dayIndex + 1)
                    set(Calendar.HOUR_OF_DAY, event.notificationHour!!)
                    set(Calendar.MINUTE, event.notificationMinute!!)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_MONTH, intervalDays)
                    }
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCodeFor(event.id, dayIndex + 1),
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
        } else {
            val calendar = Calendar.getInstance().apply {
                event.date?.split("-")?.takeIf { it.size == 3 }?.let { parts ->
                    try {
                        set(Calendar.YEAR, parts[0].toInt())
                        set(Calendar.MONTH, parts[1].toInt() - 1)
                        set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                    } catch (_: NumberFormatException) {
                        // Use current date if parsing fails
                    }
                }
                set(Calendar.HOUR_OF_DAY, event.notificationHour!!)
                set(Calendar.MINUTE, event.notificationMinute!!)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCodeFor(event.id, 0),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

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
            scheduled = true
        }
        scheduled
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun cancelEventNotification(context: Context, event: Event) {
    if (event.id == 0L) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("event_type", "event_reminder")
    }

    val repeatDays = extractRepeatingDayIndices(event.date)
    if (repeatDays.isNotEmpty()) {
        repeatDays.forEach { dayIndex ->
            cancelPendingIntent(context, alarmManager, intent, requestCodeFor(event.id, dayIndex + 1))
        }
    } else {
        cancelPendingIntent(context, alarmManager, intent, requestCodeFor(event.id, 0))
    }
}

private fun cancelPendingIntent(
    context: Context,
    alarmManager: AlarmManager,
    intent: Intent,
    requestCode: Int
) {
    val existing = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    existing?.let {
        alarmManager.cancel(it)
        it.cancel()
    }
}

private fun extractRepeatingDayIndices(date: String?): List<Int> {
    if (date.isNullOrBlank() || !date.startsWith("Every")) {
        return emptyList()
    }
    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    return days.mapIndexedNotNull { index, day -> if (date.contains(day)) index else null }
}

private fun requestCodeFor(eventId: Long, key: Int): Int {
    val hash = java.util.Objects.hash(eventId, key)
    return if (hash == Int.MIN_VALUE) 0 else kotlin.math.abs(hash)
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
