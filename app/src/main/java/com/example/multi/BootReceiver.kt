package com.example.multi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase

/**
 * Reschedules event notifications after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            scheduleDailyActivityReminder(context)
            CoroutineScope(Dispatchers.Default).launch {
                val dao = EventDatabase.getInstance(context).eventDao()
                val events = withContext(Dispatchers.IO) { dao.getEvents() }
                events.filter { it.notificationEnabled && it.notificationHour != null && it.notificationMinute != null }.forEach {
                    scheduleEventNotification(
                        context,
                        it.title,
                        it.description,
                        it.notificationHour!!,
                        it.notificationMinute!!,
                        it.date,
                        EventDateUtils.resolveNextNotificationDate(it.date)
                    )
                }
            }
        }
    }
}
