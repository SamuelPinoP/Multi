package com.example.multi

import android.content.Context
import android.content.SharedPreferences

object WeeklyGoalPreferences {
    private const val PREFS_NAME = "weekly_goal_settings"
    private const val KEY_COMPLETION_WEEK = "completion_week"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getCompletionWeek(context: Context): Int {
        return prefs(context).getInt(KEY_COMPLETION_WEEK, -1)
    }

    fun setCompletionWeek(context: Context, week: Int) {
        prefs(context).edit().putInt(KEY_COMPLETION_WEEK, week).apply()
    }
}
