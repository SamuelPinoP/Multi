package com.example.multi.util

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray

/** Stores the persisted state for the Weekly Goals mindset section (multi-card). */
object MindsetPrefs {
    private const val PREFS_NAME = "weekly_goals_prefs"

    // Multi-card storage
    private const val KEY_MINDSET_LIST = "mindset_list"                 // JSON Array of strings
    private const val KEY_MINDSET_EXPANDED_LIST = "mindset_expanded_list" // JSON Array of booleans (parallel to above)

    // Legacy single-card keys kept for backward compatibility
    private const val KEY_MINDSET_EXPANDED = "mindset_expanded"
    private const val KEY_MINDSET_TEXT = "mindset_text"

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
                setMindsets(context, listOf(legacy))
                // Default expanded state for the first card based on legacy flag
                val exp = if (p.getBoolean(KEY_MINDSET_EXPANDED, false)) listOf(true) else listOf(false)
                setExpandedStates(context, exp)
                p.edit {
                    remove(KEY_MINDSET_TEXT)
                    remove(KEY_MINDSET_EXPANDED)
                }
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

    /** Overwrite the entire mindset text list. */
    fun setMindsets(context: Context, list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs(context).edit { putString(KEY_MINDSET_LIST, arr.toString()) }
    }

    /** Parallel expanded states; list size should match getMindsets().size */
    fun getExpandedStates(context: Context): MutableList<Boolean> {
        val raw = prefs(context).getString(KEY_MINDSET_EXPANDED_LIST, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            MutableList(arr.length()) { i -> arr.optBoolean(i, false) }
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun setExpandedStates(context: Context, states: List<Boolean>) {
        val arr = JSONArray()
        states.forEach { arr.put(it) }
        prefs(context).edit { putString(KEY_MINDSET_EXPANDED_LIST, arr.toString()) }
    }

    // ---- Legacy single-card helpers (still used elsewhere safely) ----
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
        // Keep expanded list length consistent
        setExpandedStates(context, listOf(false))
    }
}

