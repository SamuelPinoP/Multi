package com.example.multi

import android.content.Context
import android.content.SharedPreferences

object GoalCelebrationPreferences {
    private const val PREFS_NAME = "goal_celebration"
    private const val KEY_EDGE_WEEK = "edge_week"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getEdgeWeek(context: Context): String? {
        return prefs(context).getString(KEY_EDGE_WEEK, null)
    }

    fun setEdgeWeek(context: Context, weekStart: String) {
        prefs(context).edit().putString(KEY_EDGE_WEEK, weekStart).apply()
    }
}
