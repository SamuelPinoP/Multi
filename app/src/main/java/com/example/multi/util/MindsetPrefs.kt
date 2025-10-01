package com.example.multi.util

import android.content.Context
import androidx.core.content.edit

/** Stores the persisted expansion state for the Weekly Goals mindset card. */
object MindsetPrefs {
    private const val PREFS_NAME = "weekly_goals_prefs"
    private const val KEY_MINDSET_EXPANDED = "mindset_expanded"

    fun isExpanded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MINDSET_EXPANDED, false)
    }

    fun setExpanded(context: Context, expanded: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_MINDSET_EXPANDED, expanded) }
    }
}
