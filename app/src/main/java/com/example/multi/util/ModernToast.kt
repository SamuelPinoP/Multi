package com.example.multi.util

import android.content.Context
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.multi.R

fun Context.showModernToast(message: String) {
    val toast = Toast(this)
    toast.duration = Toast.LENGTH_SHORT
    val textView = TextView(this).apply {
        text = message
        setTextColor(ContextCompat.getColor(this@showModernToast, R.color.white))
        setBackgroundResource(R.drawable.toast_background)
        val horizontal = (16 * resources.displayMetrics.density).toInt()
        val vertical = (8 * resources.displayMetrics.density).toInt()
        setPadding(horizontal, vertical, horizontal, vertical)
    }
    toast.view = textView
    // Position the toast closer to the system navigation bar so it doesn't
    // overlap important UI elements like floating action buttons.
    toast.setGravity(
        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        0,
        (16 * resources.displayMetrics.density).toInt()
    )
    toast.show()
}
