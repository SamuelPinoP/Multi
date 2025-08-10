package com.example.multi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.multi.data.EventDatabase
import com.example.multi.util.scheduleEventNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules event notifications after the device reboots.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = EventDatabase.getInstance(context).eventDao()
                val events = dao.getEvents()
                events.forEach { event ->
                    if (event.notificationEnabled && event.notificationHour != null && event.notificationMinute != null) {
                        val days = event.repeatDays?.split(",")?.mapNotNull { it.toIntOrNull() }
                        scheduleEventNotification(
                            context,
                            event.title,
                            event.description,
                            event.notificationHour,
                            event.notificationMinute,
                            event.date,
                            days
                        )
                    }
                }
            }
        }
    }
}
