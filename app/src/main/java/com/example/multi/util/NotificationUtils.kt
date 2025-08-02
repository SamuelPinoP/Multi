package com.example.multi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.multi.Event
import com.example.multi.EventReminderReceiver
import java.time.LocalDate
import java.util.Calendar

fun scheduleEventNotification(context: Context, event: Event, hour: Int, minute: Int) {
    val cal = Calendar.getInstance().apply {
        event.date?.let {
            val date = LocalDate.parse(it)
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
        }
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }

    val intent = Intent(context, EventReminderReceiver::class.java).apply {
        putExtra(EventReminderReceiver.EXTRA_TITLE, event.title)
        putExtra(EventReminderReceiver.EXTRA_DESC, event.description)
    }

    val pending = PendingIntent.getBroadcast(
        context,
        event.id.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
}
