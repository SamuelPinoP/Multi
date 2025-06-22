package com.example.multi

import android.graphics.Color
import android.view.LayoutInflater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Dialog displaying a MaterialCalendarView with event dates highlighted. */
@Composable
fun EventCalendarDialog(
    onDismiss: () -> Unit,
    onEvents: (String?) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val eventDates = remember { mutableStateListOf<LocalDate>() }

    LaunchedEffect(Unit) {
        val dao = EventDatabase.getInstance(context).eventDao()
        val stored = withContext(Dispatchers.IO) { dao.getEvents() }
        eventDates.clear()
        eventDates.addAll(stored.mapNotNull { it.date?.let { d -> LocalDate.parse(d) } })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onEvents(selectedDate?.toString()) }) {
                Text("Events")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            AndroidView(
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.dialog_material_calendar, null) as MaterialCalendarView
                },
                update = { view ->
                    view.setOnDateChangedListener { _, day, _ ->
                        selectedDate = day.date
                    }
                    view.removeDecorators()
                    if (eventDates.isNotEmpty()) {
                        val days = eventDates.map { CalendarDay.from(it) }.toSet()
                        view.addDecorator(object : DayViewDecorator {
                            override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)
                            override fun decorate(facade: DayViewFacade) {
                                facade.addSpan(DotSpan(8f, Color.GREEN))
                            }
                        })
                    }
                }
            )
        }
    )
}
