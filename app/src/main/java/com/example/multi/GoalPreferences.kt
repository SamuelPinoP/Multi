package com.example.multi

import android.content.Context
import android.content.SharedPreferences

object GoalPreferences {
    private const val PREFS_NAME = "settings"
    private const val KEY_COMPLETED_WEEK = "completed_week"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCompletedWeek(context: Context): Int {
        return prefs(context).getInt(KEY_COMPLETED_WEEK, -1)
    }

    fun setCompletedWeek(context: Context, week: Int) {
        prefs(context).edit().putInt(KEY_COMPLETED_WEEK, week).apply()
    }
}
