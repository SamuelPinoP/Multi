package com.example.multi

import android.content.Context
import androidx.activity.ComponentActivity

/**
 * Persists the last activity visited by the user so that the app can resume
 * from that screen on the next launch.
 */
object LastVisitedActivityPrefs {
    private const val PREFS_NAME = "last_visited_activity"
    private const val KEY_CLASS_NAME = "activity_class_name"

    fun setLastActivity(activity: ComponentActivity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CLASS_NAME, activity::class.java.name)
            .apply()
    }

    fun getLastActivityClass(context: Context): Class<out ComponentActivity>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val className = prefs.getString(KEY_CLASS_NAME, null) ?: return null
        return runCatching {
            Class.forName(className).asSubclass(ComponentActivity::class.java)
        }.getOrNull()
    }
}
