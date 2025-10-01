package com.example.multi.util

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray

/** Stores persisted state for the Weekly Goals "Mindset" section (multi-card). */
object MindsetPrefs {
    private const val PREFS_NAME = "weekly_goals_prefs"
    private const val KEY_MINDSET_LIST = "mindset_list"          // JSON Array of strings
    private const val KEY_MINDSET_EXPANDED = "mindset_expanded"   // (legacy single-card)
    private const val KEY_MINDSET_TEXT = "mindset_text"           // (legacy single-card)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Return all saved mindsets as a list of strings (order preserved). */
    fun getMindsets(context: Context): MutableList<String> {
        val p = prefs(context)
        val raw = p.getString(KEY_MINDSET_LIST, null)

        // Migrate legacy single value if needed
        if (raw.isNullOrEmpty()) {
            val legacy = p.getString(KEY_MINDSET_TEXT, null)
            if (!legacy.isNullOrEmpty()) {
                // Store as list and clear legacy
                setMindsets(context, listOf(legacy))
                p.edit { remove(KEY_MINDSET_TEXT) }
                return mutableListOf(legacy)
            }
            return mutableListOf()
        }

        val arr = JSONArray(raw)
        val out = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            out.add(arr.optString(i, ""))
        }
        return out
    }

    /** Overwrite the entire list. */
    fun setMindsets(context: Context, list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs(context).edit { putString(KEY_MINDSET_LIST, arr.toString()) }
    }

    /** Convenience: add one item to the end. */
    fun addMindset(context: Context, text: String) {
        val list = getMindsets(context)
        list.add(text)
        setMindsets(context, list)
    }

    /** Convenience: remove by index (no-op if out of bounds). */
    fun removeMindset(context: Context, index: Int) {
        val list = getMindsets(context)
        if (index in list.indices) {
            list.removeAt(index)
            setMindsets(context, list)
        }
    }

    // ---- Legacy single-card helpers kept for backward compatibility ----
    fun isExpanded(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MINDSET_EXPANDED, false)

    fun setExpanded(context: Context, expanded: Boolean) {
        prefs(context).edit { putBoolean(KEY_MINDSET_EXPANDED, expanded) }
    }

    fun getText(context: Context): String {
        val list = getMindsets(context)
        return if (list.isNotEmpty()) list.first() else ""
    }

    fun setText(context: Context, text: String) {
        setMindsets(context, listOf(text))
    }
}
