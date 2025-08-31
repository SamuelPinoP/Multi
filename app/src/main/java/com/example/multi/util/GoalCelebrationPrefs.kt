package com.example.multi.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

/**
 * Stores whether the “weekly celebration” was activated for the *current* week.
 * Weeks start on Sunday to match your WeeklyGoals logic.
 */
object GoalCelebrationPrefs {
    private const val PREFS_NAME = "weekly_goals_prefs"
    private const val KEY_LAST_CELEBRATED_WEEK_START = "last_celebrated_week_start"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sundayStart(date: LocalDate = LocalDate.now()): LocalDate {
        // dayOfWeek.value: Mon=1..Sun=7  → %7 turns Sun into 0
        val shift = (date.dayOfWeek.value % 7).toLong()
        return date.minusDays(shift)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun currentWeekToken(): String = sundayStart().toString() // ISO yyyy-MM-dd for Sunday

    /** True if the stored Sunday equals *this* week’s Sunday (i.e., already activated). */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val last = prefs.getString(KEY_LAST_CELEBRATED_WEEK_START, null)
        return last == currentWeekToken()
    }

    /** Mark the current week as “celebration active”. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun activateForCurrentWeek(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_CELEBRATED_WEEK_START, currentWeekToken()).apply()
    }

    /** Optional: clear the flag (handy for testing). */
    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_LAST_CELEBRATED_WEEK_START).apply()
    }
}