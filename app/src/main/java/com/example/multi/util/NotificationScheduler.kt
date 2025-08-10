package com.example.multi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.multi.NotificationReceiver
import java.util.Calendar

/**
 * Schedules notifications for events. If [repeatDays] is provided, a weekly
 * repeating alarm is set for each specified day of week (using
 * [Calendar.DAY_OF_WEEK] values). Otherwise a one-time alarm is scheduled for
 * the provided [date] or the next occurrence of the time.
 */
fun scheduleEventNotification(
    context: Context,
    title: String,
    description: String,
    hour: Int,
    minute: Int,
    date: String? = null,
    repeatDays: List<Int>? = null
) : Boolean {
    return try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (repeatDays != null && repeatDays.isNotEmpty()) {
            repeatDays.forEach { day ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    while (get(Calendar.DAY_OF_WEEK) != day || timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
                val requestCode = (title.hashCode() + description.hashCode() + day).hashCode()
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("title", title)
                    putExtra("description", description)
                    putExtra("event_type", "event_reminder")
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
        } else {
            val calendar = Calendar.getInstance().apply {
                if (!date.isNullOrBlank()) {
                    val parts = date.split("-")
                    if (parts.size == 3) {
                        try {
                            set(Calendar.YEAR, parts[0].toInt())
                            set(Calendar.MONTH, parts[1].toInt() - 1)
                            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                        } catch (_: NumberFormatException) {
                            // ignore and keep current date
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

            val requestCode = (System.currentTimeMillis() + title.hashCode()).toInt()
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
                putExtra("event_type", "event_reminder")
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
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
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
