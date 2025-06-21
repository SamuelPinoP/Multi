package com.example.multi

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(millis: Long): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .toString()
    } else {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fmt.format(Date(millis))
    }
}

fun daysUntil(timestamp: Long, periodDays: Int): Int {
    val elapsed = System.currentTimeMillis() - timestamp
    val remainingMillis = periodDays * 24L * 60 * 60 * 1000 - elapsed
    return (remainingMillis / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
}
