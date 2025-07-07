package com.example.multi.util

import android.content.Context

/** Utility for persisting small app preferences. */
object PreferenceUtils {
    private const val PREFS_NAME = "multi_prefs"
    private const val KEY_LAST_ACTIVITY = "last_activity"

    /** Record the fully qualified class name of the last opened activity. */
    fun setLastActivity(context: Context, className: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_ACTIVITY, className)
            .apply()
    }

    /** Retrieve the last opened activity class name, or null if not set. */
    fun getLastActivity(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_ACTIVITY, null)
    }
}
