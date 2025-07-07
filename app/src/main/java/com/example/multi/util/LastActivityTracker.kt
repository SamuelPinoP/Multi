package com.example.multi.util

import android.app.Activity
import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "last_activity_prefs"
private const val KEY_LAST_ACTIVITY = "last_activity"

/** Utility for persisting and restoring the last opened [Activity] class. */
object LastActivityTracker {
    /** Save the given [activity] class name as the last opened activity. */
    fun saveLastActivity(context: Context, activity: Class<out Activity>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LAST_ACTIVITY, activity.name) }
    }

    /** Retrieve the last opened activity class if available. */
    fun getLastActivityClass(context: Context): Class<out Activity>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_LAST_ACTIVITY, null) ?: return null
        return try {
            @Suppress("UNCHECKED_CAST")
            Class.forName(name) as Class<out Activity>
        } catch (e: ClassNotFoundException) {
            null
        }
    }
}
