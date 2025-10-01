package com.example.multi.util

import android.content.Context
import androidx.core.content.edit

/** Stores persisted state for the Weekly Goals "Mindset" card. */
object MindsetPrefs {
    private const val PREFS_NAME = "weekly_goals_prefs"
    private const val KEY_MINDSET_EXPANDED = "mindset_expanded"
    private const val KEY_MINDSET_TEXT = "mindset_text"

    // Expansion state
    fun isExpanded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MINDSET_EXPANDED, false)
    }

    fun setExpanded(context: Context, expanded: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_MINDSET_EXPANDED, expanded) }
    }

    // Mindset text
    fun getText(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MINDSET_TEXT, "") ?: ""
    }

    fun setText(context: Context, text: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_MINDSET_TEXT, text) }
    }

    fun clearText(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_MINDSET_TEXT) }
    }
}
