package com.example.multi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.multi.data.EventDatabase

/**
 * Re-schedules event notifications after the device boots.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = EventDatabase.getInstance(context).eventDao()
                val events = dao.getEvents()
                events.forEach { event ->
                    if (event.notificationEnabled && event.notificationHour != null && event.notificationMinute != null) {
                        scheduleEventNotification(
                            context,
                            event.title,
                            event.description,
                            event.notificationHour,
                            event.notificationMinute,
                            event.date
                        )
                    }
                }
            }
        }
    }
}

