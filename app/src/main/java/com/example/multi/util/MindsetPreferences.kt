package com.example.multi.util

import android.content.Context

/**
 * Stores the persisted state for the weekly mindset accordion, including the
 * expanded/collapsed state and the guiding message text.
 */
object MindsetPreferences {
    private const val PREFS_NAME = "weekly_mindset"
    private const val KEY_EXPANDED = "expanded"
    private const val KEY_MESSAGE = "message"

    /** Returns whether the accordion should start expanded. */
    fun isExpanded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_EXPANDED, true)
    }

    /** Persists the accordion expanded state. */
    fun setExpanded(context: Context, expanded: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_EXPANDED, expanded).apply()
    }

    /** Returns the saved mindset message or an empty string if missing. */
    fun getMessage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MESSAGE, "") ?: ""
    }

    /** Persists the mindset message text. */
    fun setMessage(context: Context, message: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MESSAGE, message).apply()
    }
}
