package com.example.multi

import android.content.Context
import android.content.SharedPreferences

object ThemePreferences {
    private const val PREFS_NAME = "settings"
    private const val KEY_DARK_THEME = "dark_theme"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isDarkTheme(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkTheme(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    fun toggle(context: Context): Boolean {
        val newValue = !isDarkTheme(context)
        setDarkTheme(context, newValue)
        return newValue
    }
}
