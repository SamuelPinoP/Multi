package com.example.multi.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.multi.Event
import java.time.LocalDateTime
import java.time.ZoneId
import java.text.SimpleDateFormat
import java.util.Locale

object EventNotificationScheduler {
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_DESC = "extra_desc"
    const val EXTRA_ID = "extra_id"

    fun schedule(context: Context, event: Event) {
        val date = event.date ?: return
        val time = event.notifyTime ?: return
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.multi.EventNotificationReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, event.title)
            putExtra(EXTRA_DESC, event.description)
            putExtra(EXTRA_ID, event.id)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            event.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dt = LocalDateTime.parse("${date}T${time}")
            dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            fmt.parse("${date}T${time}")!!.time
        }
        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
    }

    fun cancel(context: Context, id: Long) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.multi.EventNotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pending)
    }
}
