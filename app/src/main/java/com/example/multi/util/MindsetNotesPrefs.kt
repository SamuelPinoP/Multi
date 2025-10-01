package com.example.multi.util

import android.content.Context
import androidx.core.content.edit

/** Persists user-created mindset notes for the Weekly Goals screen. */
object MindsetNotesPrefs {
    private const val PREFS_NAME = "weekly_goals_prefs"
    private const val KEY_MINDSET_NOTES = "mindset_notes"
    private const val DELIMITER = "\u001F" // unit separator

    fun getNotes(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_MINDSET_NOTES, null)
        if (stored.isNullOrEmpty()) return emptyList()
        return stored.split(DELIMITER).mapNotNull { it.takeIf(String::isNotBlank) }
    }

    fun saveNotes(context: Context, notes: List<String>) {
        val sanitized = notes.map { it.trim() }.filter { it.isNotEmpty() }
        val serialized = sanitized.joinToString(DELIMITER)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_MINDSET_NOTES, serialized) }
    }
}
