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
private val daysOfWeek = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

fun scheduleEventNotification(
    context: Context,
    eventId: Long,
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
        val selectedDayIndexes = resolveRepeatingDayIndexes(date)
        if (selectedDayIndexes.isNotEmpty()) {
            val intervalDays = if (date.startsWith("Every other")) 14 else 7
            selectedDayIndexes.forEach { index ->
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
                val requestCode = buildRequestCode(eventId, index)
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
        } else {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
                putExtra("event_type", "event_reminder")
            }
            val requestCode = buildRequestCode(eventId)
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

fun cancelEventNotifications(
    context: Context,
    eventId: Long,
    date: String?
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val indexes = resolveRepeatingDayIndexes(date)
    val requestCodes = if (indexes.isNotEmpty()) {
        indexes.map { buildRequestCode(eventId, it) }
    } else {
        listOf(buildRequestCode(eventId))
    }
    requestCodes.forEach { code ->
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}

private fun resolveRepeatingDayIndexes(date: String?): List<Int> {
    if (date.isNullOrBlank() || !date.startsWith("Every")) return emptyList()
    return daysOfWeek.mapIndexedNotNull { index, day ->
        if (date.contains(day)) index else null
    }
}

private fun buildRequestCode(eventId: Long, dayIndex: Int? = null): Int {
    val base = eventId.hashCode() * 31
    return if (dayIndex != null) base + dayIndex else base
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
