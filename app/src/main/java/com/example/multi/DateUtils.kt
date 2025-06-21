package com.example.multi

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneOffset

/** Formats a timestamp (milliseconds) into a yyyy-MM-dd date string. */
fun formatDate(millis: Long): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
    } else {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fmt.format(Date(millis))
    }
}

/** Returns remaining days before a trashed note expires. */
fun daysRemaining(deletedTime: Long, retentionDays: Int = 30): Int {
    val expire = deletedTime + retentionDays * 24L * 60 * 60 * 1000
    val remaining = expire - System.currentTimeMillis()
    return (remaining / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
}
