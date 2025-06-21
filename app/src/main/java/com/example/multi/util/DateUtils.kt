package com.example.multi.util

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toDateString(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("M/d/yyyy"))
} else {
    val fmt = SimpleDateFormat("M/d/yyyy", Locale.getDefault())
    fmt.format(Date(this))
}
