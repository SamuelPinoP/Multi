package com.example.multi

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.multi.data.EventDatabase
import kotlinx.coroutines.runBlocking

class LastNoteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_last_note)
            val intent = Intent(context, LastNoteWidgetProvider::class.java).apply {
                action = ACTION_OPEN_LAST_NOTE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_OPEN_LAST_NOTE) {
            runBlocking {
                val dao = EventDatabase.getInstance(context).noteDao()
                val note = dao.getNotes().firstOrNull()
                if (note != null) {
                    val open = Intent(context, NoteEditorActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(EXTRA_NOTE_ID, note.id)
                        putExtra(EXTRA_NOTE_HEADER, note.header)
                        putExtra(EXTRA_NOTE_CONTENT, note.content)
                        putExtra(EXTRA_NOTE_CREATED, note.created)
                        putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                        putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                        putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                    }
                    context.startActivity(open)
                } else {
                    val notesIntent = Intent(context, NotesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(notesIntent)
                }
            }
        }
    }

    companion object {
        private const val ACTION_OPEN_LAST_NOTE = "com.example.multi.OPEN_LAST_NOTE"
    }
}
