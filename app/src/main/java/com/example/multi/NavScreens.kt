package com.example.multi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel

@Composable
fun EventsRoute(navController: NavController) {
    val context = LocalContext.current
    val events = remember { mutableStateListOf<Event>() }
    val notes = remember { mutableStateMapOf<Long, Note>() }

    LaunchedEffect(Unit) {
        val db = EventDatabase.getInstance(context)
        val eventsStored = withContext(Dispatchers.IO) { db.eventDao().getEvents() }
        val notesStored = withContext(Dispatchers.IO) { db.noteDao().getNotes() }
        events.clear(); events.addAll(eventsStored.map { it.toModel() })
        notes.clear()
        notesStored.map { it.toModel() }.forEach { note ->
            note.attachmentUri?.takeIf { it.startsWith("event:") && note.content.isNotBlank() }?.let {
                val eventId = it.removePrefix("event:").toLongOrNull()
                if (eventId != null) notes[eventId] = note
            }
        }
    }

    SegmentScreen(
        title = "Events",
        onBack = { navController.popBackStack() },
        onClose = { navController.popBackStack() }
    ) {
        EventsScreen(events, notes, null)
    }
}

@Composable
fun WeeklyGoalsRoute(navController: NavController) {
    SegmentScreen(
        title = "Weekly Goals",
        onBack = { navController.popBackStack() },
        onClose = { navController.popBackStack() }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Weekly Goals Screen")
        }
    }
}

@Composable
fun NotesRoute(navController: NavController) {
    SegmentScreen(
        title = "Notes",
        onBack = { navController.popBackStack() },
        onClose = { navController.popBackStack() }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Notes Screen")
        }
    }
}

