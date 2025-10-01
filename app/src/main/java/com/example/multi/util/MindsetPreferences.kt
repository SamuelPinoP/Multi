package com.example.multi.util

import android.content.Context

/**
 * Persists the expanded / collapsed state of the weekly mindset card so the
 * UI re-opens exactly how the user left it.
 */
object MindsetPreferences {
    private const val PREFS_NAME = "weekly_mindset"
    private const val KEY_EXPANDED = "mindset_expanded"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isExpanded(context: Context): Boolean =
        prefs(context).getBoolean(KEY_EXPANDED, false)

    fun setExpanded(context: Context, expanded: Boolean) {
        prefs(context).edit().putBoolean(KEY_EXPANDED, expanded).apply()
    }
}
