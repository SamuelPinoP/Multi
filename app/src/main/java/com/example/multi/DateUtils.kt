package com.example.multi

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneOffset

/** Utility functions for formatting dates and calculating time. */
object DateUtils {
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /** Format epoch milliseconds as yyyy-MM-dd. */
    fun formatDate(epochMillis: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .toString()
        } else {
            fmt.format(Date(epochMillis))
        }
    }

    /** Days remaining given a future timestamp. */
    fun daysRemaining(futureMillis: Long): Long {
        val remaining = (futureMillis - System.currentTimeMillis()) / (24L * 60 * 60 * 1000)
        return if (remaining < 0) 0 else remaining
    }
}
