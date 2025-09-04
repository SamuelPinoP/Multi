package com.example.multi

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

class LastNoteWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.last_note_widget)
            runBlocking {
                val dao = EventDatabase.getInstance(context).noteDao()
                val note = withContext(Dispatchers.IO) { dao.getLastOpened() }?.toModel()
                if (note != null) {
                    val intent = Intent(context, NoteEditorActivity::class.java).apply {
                        putExtra(EXTRA_NOTE_ID, note.id)
                        putExtra(EXTRA_NOTE_HEADER, note.header)
                        putExtra(EXTRA_NOTE_CONTENT, note.content)
                        putExtra(EXTRA_NOTE_CREATED, note.created)
                        putExtra(EXTRA_NOTE_SCROLL, note.scroll)
                        putExtra(EXTRA_NOTE_CURSOR, note.cursor)
                        putExtra(EXTRA_NOTE_ATTACHMENT_URI, note.attachmentUri)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val pending = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pending)
                    val title = if (note.header.isNotBlank()) {
                        note.header
                    } else {
                        note.content.lineSequence().firstOrNull()?.trim()?.take(40) ?: context.getString(R.string.app_name)
                    }
                    views.setTextViewText(R.id.widget_note_header, title)
                } else {
                    views.setTextViewText(R.id.widget_note_header, context.getString(R.string.no_notes))
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
