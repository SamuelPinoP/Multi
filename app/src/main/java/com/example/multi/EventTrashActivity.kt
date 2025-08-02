package com.example.multi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing deleted events. */
class EventTrashActivity : SegmentActivity("Trash") {
    @Composable
    override fun SegmentContent() {
        val context = LocalContext.current
        val events = remember { mutableStateListOf<TrashedEvent>() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            val dao = EventDatabase.getInstance(context).trashedEventDao()
            val threshold = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            withContext(Dispatchers.IO) { dao.deleteExpired(threshold) }
            val stored = withContext(Dispatchers.IO) { dao.getEvents() }
            events.clear(); events.addAll(stored.map { it.toModel() })
        }

        TrashList(
            items = events,
            deletedTime = { it.deleted },
            cardModifier = { Modifier.fillMaxWidth() },
            onRestore = { event ->
                scope.launch {
                    val db = EventDatabase.getInstance(context)
                    withContext(Dispatchers.IO) {
                        db.eventDao().insert(
                            Event(
                                title = event.title,
                                description = event.description,
                                date = event.date,
                                address = event.address
                            ).toEntity()
                        )
                        db.trashedEventDao().delete(event.toEntity())
                    }
                    events.remove(event)
                }
            },
            onDelete = { event ->
                scope.launch {
                    val dao = EventDatabase.getInstance(context).trashedEventDao()
                    withContext(Dispatchers.IO) { dao.delete(event.toEntity()) }
                    events.remove(event)
                }
            }
        ) { event ->
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            if (event.description.isNotBlank()) {
                Text(event.description)
            }
            event.date?.let {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
            event.address?.takeIf { it.isNotBlank() }?.let { addr ->
                Text(
                    addr,
                    color = Color.Blue,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable {
                            val uri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(addr))
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}
