package com.example.multi.util

import android.content.Context
import androidx.activity.ComponentActivity

/**
 * Stores and retrieves the last [ComponentActivity] visited by the user so the
 * app can restore it on the next launch.
 */
object LastVisitedPreferences {
    private const val PREFS_NAME = "last_visited_activity"
    private const val KEY_LAST_ACTIVITY = "last_activity_class"

    /** Saves the fully qualified class name of the provided [activityClass]. */
    fun setLastVisitedActivity(context: Context, activityClass: Class<out ComponentActivity>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_ACTIVITY, activityClass.name).apply()
    }

    /**
     * Returns the previously saved activity class or `null` if it is missing or
     * no longer available.
     */
    fun getLastVisitedActivity(context: Context): Class<out ComponentActivity>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val className = prefs.getString(KEY_LAST_ACTIVITY, null) ?: return null
        return runCatching {
            Class.forName(className).let { activityClass ->
                if (ComponentActivity::class.java.isAssignableFrom(activityClass)) {
                    @Suppress("UNCHECKED_CAST")
                    activityClass as Class<out ComponentActivity>
                } else {
                    null
                }
            }
        }.getOrNull()
    }
}
