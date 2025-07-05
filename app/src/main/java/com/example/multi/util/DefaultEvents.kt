package com.example.multi.util

import com.example.multi.Event
import com.example.multi.data.EventDao
import com.example.multi.data.toEntity

/** Insert a small set of notable events if the events table is empty. */
suspend fun insertDefaultEventsIfEmpty(dao: EventDao) {
    if (dao.getEvents().isNotEmpty()) return
    val defaults = listOf(
        Event(title = "New Year's Day", description = "", date = "2024-01-01"),
        Event(title = "Presidents Day", description = "", date = "2024-02-19"),
        Event(title = "Super Bowl", description = "", date = "2024-02-11"),
        Event(title = "Independence Day", description = "", date = "2024-07-04")
    )
    defaults.forEach { dao.insert(it.toEntity()) }
}
