package com.example.multi

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

class LastNoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            val dao = EventDatabase.getInstance(context).noteDao()
            val note = dao.getLastNote()?.toModel()
            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.last_note_widget).apply {
                    setTextViewText(R.id.widget_text, note?.header ?: context.getString(R.string.open_last_note))
                    val intent = Intent(context, LastNoteWidget::class.java).apply { action = ACTION_OPEN_LAST_NOTE }
                    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_OPEN_LAST_NOTE) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = EventDatabase.getInstance(context).noteDao()
                val note = dao.getLastNote()?.toModel()
                val openIntent = if (note != null) {
                    Intent(context, NoteEditorActivity::class.java).apply {
                        putExtra(EXTRA_NOTE_ID, note.id)
                        putExtra(EXTRA_NOTE_HEADER, note.header)
                        putExtra(EXTRA_NOTE_CONTENT, note.content)
                        putExtra(EXTRA_NOTE_CREATED, note.created)
                        putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                        putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                        putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } else {
                    Intent(context, NotesActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                context.startActivity(openIntent)
            }
        }
    }

    companion object {
        private const val ACTION_OPEN_LAST_NOTE = "com.example.multi.OPEN_LAST_NOTE"
    }
}
