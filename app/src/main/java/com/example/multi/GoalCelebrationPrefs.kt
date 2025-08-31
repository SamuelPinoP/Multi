package com.example.multi

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Stores the week number when all goals were completed so we can keep showing
 * celebration effects until the next week starts.
 */
object GoalCelebrationPrefs {
    private const val PREFS_NAME = "goal_celebration"
    private const val KEY_WEEK = "celebration_week"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @RequiresApi(Build.VERSION_CODES.O)
    fun isActive(context: Context): Boolean {
        val stored = prefs(context).getInt(KEY_WEEK, -1)
        val current = currentWeek()
        return stored == current
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun activateForCurrentWeek(context: Context) {
        prefs(context).edit().putInt(KEY_WEEK, currentWeek()).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_WEEK).apply()
    }
}
