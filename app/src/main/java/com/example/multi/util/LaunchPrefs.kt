package com.example.multi.util

import android.content.Context
import androidx.activity.ComponentActivity

private const val PREFS_NAME = "launch_prefs"
private const val KEY_LAST_ACTIVITY = "last_activity"

object LaunchPrefs {
    fun setLastActivity(activity: ComponentActivity) {
        val prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_ACTIVITY, activity::class.java.name).apply()
    }

    fun getLastActivityClass(context: Context): Class<out ComponentActivity>? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_LAST_ACTIVITY, null) ?: return null
        return try {
            @Suppress("UNCHECKED_CAST")
            Class.forName(name) as Class<out ComponentActivity>
        } catch (e: Exception) {
            null
        }
    }
}
