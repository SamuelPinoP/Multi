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
    // Push the toast far enough above the bottom of the screen so it does not
    // overlap buttons anchored there (e.g. "Add Event" or "Calendar").  Using
    // a larger offset makes the message feel attached to the bottom without
    // obscuring any UI elements.
    toast.setGravity(
        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        0,
        (112 * resources.displayMetrics.density).toInt()
    )
    toast.show()
}
