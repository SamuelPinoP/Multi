package com.example.multi.util

import android.content.Context

/**
 * Stores UI preferences for the weekly mindset accordion.
 */
object MindsetPreferences {
    private const val PREFS_NAME = "mindset_preferences"
    private const val KEY_EXPANDED = "mindset_expanded"
    private const val KEY_MESSAGE = "mindset_message"
    private const val DEFAULT_MESSAGE = "Loving Jesus"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isExpanded(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_EXPANDED, true)
    }

    fun setExpanded(context: Context, expanded: Boolean) {
        prefs(context).edit().putBoolean(KEY_EXPANDED, expanded).apply()
    }

    fun getMessage(context: Context): String {
        return prefs(context).getString(KEY_MESSAGE, DEFAULT_MESSAGE) ?: DEFAULT_MESSAGE
    }

    fun setMessage(context: Context, message: String) {
        prefs(context).edit().putString(KEY_MESSAGE, message).apply()
    }
}
