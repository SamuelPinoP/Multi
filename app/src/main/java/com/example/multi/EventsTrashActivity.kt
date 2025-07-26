package com.example.multi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toEntity
import com.example.multi.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Activity showing deleted events. */
class EventsTrashActivity : SegmentActivity("Event Trash") {
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

        Box(modifier = Modifier.fillMaxSize()) {
            if (events.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Trash is empty",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events) { ev ->
                        val daysLeft = ((ev.deleted + 30L * 24 * 60 * 60 * 1000 - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
                        ElevatedCard(
                            elevation = CardDefaults.elevatedCardElevation(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(ev.title, style = MaterialTheme.typography.titleMedium)
                                if (ev.description.isNotBlank()) {
                                    Text(ev.description, style = MaterialTheme.typography.bodyMedium)
                                }
                                ev.date?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Days remaining: $daysLeft",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {
                                        scope.launch {
                                            val db = EventDatabase.getInstance(context)
                                            withContext(Dispatchers.IO) {
                                                db.eventDao().insert(
                                                    Event(ev.title, ev.description, ev.date).toEntity()
                                                )
                                                db.trashedEventDao().delete(ev.toEntity())
                                            }
                                            events.remove(ev)
                                        }
                                    }) { Text("Restore") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        scope.launch {
                                            val dao = EventDatabase.getInstance(context).trashedEventDao()
                                            withContext(Dispatchers.IO) { dao.delete(ev.toEntity()) }
                                            events.remove(ev)
                                        }
                                    }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
