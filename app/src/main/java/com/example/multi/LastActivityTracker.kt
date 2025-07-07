package com.example.multi

import android.content.Context
import androidx.activity.ComponentActivity

object LastActivityTracker {
    private const val PREF_NAME = "last_activity_prefs"
    private const val KEY_LAST_ACTIVITY = "last_activity"

    fun record(context: Context, activityClass: Class<out ComponentActivity>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_ACTIVITY, activityClass.name)
            .apply()
    }

    fun getLastActivityClassName(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_ACTIVITY, null)
    }
}
