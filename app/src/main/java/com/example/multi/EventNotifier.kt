package com.example.multi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

object EventNotifier {
    fun schedule(context: Context, event: Event) {
        val dateStr = event.date ?: return
        val date = try { LocalDate.parse(dateStr) } catch (_: DateTimeParseException) { return }
        val time = date.atTime(11, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = Intent(context, EventNotificationReceiver::class.java).apply {
            putExtra("title", event.title)
            putExtra("description", event.description)
            putExtra("id", event.id)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
    }

    fun cancel(context: Context, id: Long) {
        val intent = Intent(context, EventNotificationReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pi)
    }
}
