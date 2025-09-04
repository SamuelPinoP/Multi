package com.example.multi

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Home screen widget opening the most recently viewed note. */
class LastNoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val clickIntent = Intent(context, LastNoteWidget::class.java).apply { action = ACTION_OPEN }
            val pending = PendingIntent.getBroadcast(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val views = RemoteViews(context.packageName, R.layout.last_note_widget).apply {
                setOnClickPendingIntent(R.id.widget_root, pending)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_OPEN) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = EventDatabase.getInstance(context).noteDao()
                val entity = dao.getNotes().firstOrNull()
                if (entity != null) {
                    val note = entity.toModel()
                    val openIntent = Intent(context, NoteEditorActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(EXTRA_NOTE_ID, note.id)
                        putExtra(EXTRA_NOTE_HEADER, note.header)
                        putExtra(EXTRA_NOTE_CONTENT, note.content)
                        putExtra(EXTRA_NOTE_CREATED, note.created)
                        putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                        putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                        putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                    }
                    context.startActivity(openIntent)
                } else {
                    val openList = Intent(context, NotesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(openList)
                }
            }
        }
    }

    companion object {
        private const val ACTION_OPEN = "com.example.multi.action.OPEN_LAST_NOTE"
    }
}

