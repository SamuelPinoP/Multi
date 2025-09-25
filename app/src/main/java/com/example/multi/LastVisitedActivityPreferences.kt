package com.example.multi

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity

object LastVisitedActivityPreferences {
    private const val PREFS_NAME = "settings"
    private const val KEY_LAST_ACTIVITY = "last_activity"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun remember(activity: ComponentActivity) {
        prefs(activity).edit().putString(KEY_LAST_ACTIVITY, activity::class.java.name).apply()
    }

    fun getLastActivityClass(context: Context): Class<out ComponentActivity>? {
        val className = prefs(context).getString(KEY_LAST_ACTIVITY, null) ?: return null
        return try {
            Class.forName(className).asSubclass(ComponentActivity::class.java)
        } catch (_: ClassNotFoundException) {
            null
        } catch (_: ClassCastException) {
            null
        }
    }
}
