package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.multi.data.EventDatabase
import com.example.multi.util.toLocalDateOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Activity showing events on a calendar view. */
class EventsCalendarActivity : SegmentActivity("Events Calendar") {
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    override fun SegmentContent() {
        EventsCalendarScreen()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EventsCalendarScreen() {
    val context = LocalContext.current
    var dates by remember { mutableStateOf(setOf<LocalDate>()) }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        dates = stored.mapNotNull { it.date?.toLocalDateOrNull() }.toSet()
    }

    EventsCalendarView(dates)
}
