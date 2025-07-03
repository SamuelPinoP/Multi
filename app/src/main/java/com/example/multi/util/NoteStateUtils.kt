package com.example.multi.util

import android.content.Context

fun Context.saveNoteState(noteId: Long, scroll: Int, cursor: Int) {
    val prefs = getSharedPreferences("note_state", Context.MODE_PRIVATE)
    prefs.edit()
        .putInt("scroll_$noteId", scroll)
        .putInt("cursor_$noteId", cursor)
        .apply()
}

fun Context.loadNoteState(noteId: Long): Pair<Int, Int> {
    val prefs = getSharedPreferences("note_state", Context.MODE_PRIVATE)
    val scroll = prefs.getInt("scroll_$noteId", 0)
    val cursor = prefs.getInt("cursor_$noteId", 0)
    return scroll to cursor
}
