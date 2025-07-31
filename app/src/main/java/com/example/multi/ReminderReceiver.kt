package com.example.multi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val id = intent.getLongExtra(EXTRA_ID, 0L)
        createChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText("Event reminder")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "event_reminders"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ID = "id"

        fun createChannel(context: Context) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Event Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                mgr.createNotificationChannel(channel)
            }
        }

        fun pendingIntent(context: Context, event: Event): PendingIntent {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_TITLE, event.title)
                putExtra(EXTRA_ID, event.id)
            }
            return PendingIntent.getBroadcast(
                context,
                event.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
