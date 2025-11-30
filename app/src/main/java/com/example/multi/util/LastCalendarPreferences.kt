package com.example.multi.util

import android.content.Context
import androidx.activity.ComponentActivity

/**
 * Stores and retrieves the last calendar-related [ComponentActivity] so the
 * calendar medallion entry point can jump straight to the user's preferred
 * view.
 */
object LastCalendarPreferences {
    private const val PREFS_NAME = "last_calendar_activity"
    private const val KEY_LAST_CALENDAR = "last_calendar_class"

    /** Saves the fully qualified class name of the provided [activityClass]. */
    fun setLastCalendar(context: Context, activityClass: Class<out ComponentActivity>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_CALENDAR, activityClass.name).apply()
    }

    /**
     * Returns the previously saved calendar activity class or `null` if it is
     * missing or no longer available.
     */
    fun getLastCalendar(context: Context): Class<out ComponentActivity>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val className = prefs.getString(KEY_LAST_CALENDAR, null) ?: return null
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
