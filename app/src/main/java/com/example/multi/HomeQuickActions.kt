package com.example.multi

import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.ui.components.SectionHeader
import com.example.multi.ui.theme.MultiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The shortcut deck under the medallion. Four segment-accented tiles, each
 * carrying a live stat so the space earns its keep instead of being four bare
 * navigation buttons floating in whitespace.
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    height: Dp = 104.dp,
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val database = remember(appContext) { EventDatabase.getInstance(appContext) }
    val scope = rememberCoroutineScope()
    val ext = MultiTheme.extended
    val spacing = MultiTheme.spacing

    val notesCount by remember(database) { database.noteDao().observeCount() }
        .collectAsStateWithLifecycle(initialValue = 0)
    val eventSummary by remember(database) {
        database.eventDao().observeEvents().map(::summarizeEvents)
    }.collectAsStateWithLifecycle(initialValue = EventSummary())
    val goalPercent by remember(database) {
        database.weeklyGoalDao().observeGoals().map { goals ->
            var done = 0
            var total = 0
            goals.map { it.toModel() }.forEach { g ->
                val freq = g.frequency.coerceAtLeast(0)
                done += g.dayStates.count { it == 'C' }.coerceAtMost(freq)
                total += freq
            }
            if (total <= 0) 0 else done * 100 / total
        }
    }.collectAsStateWithLifecycle(initialValue = 0)

    val todayLabel = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d")) }

    Column(modifier = modifier) {
        SectionHeader(
            text = "Jump in",
            modifier = Modifier.padding(horizontal = spacing.xl),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuickTile(
                modifier = Modifier.weight(1f),
                label = "Notes",
                stat = if (notesCount == 1) "1 note" else "$notesCount notes",
                icon = Icons.Default.Description,
                container = ext.notes.container,
                content = ext.notes.color,
                height = height,
            ) {
                scope.launch {
                    val lastNote = withContext(Dispatchers.IO) {
                        database.noteDao().getNotes().firstOrNull()?.toModel()
                    }
                    if (lastNote != null) {
                        context.startActivity(
                            Intent(context, NoteEditorActivity::class.java).apply {
                                putExtra(EXTRA_NOTE_ID, lastNote.id)
                                putExtra(EXTRA_NOTE_HEADER, lastNote.header)
                                putExtra(EXTRA_NOTE_CONTENT, lastNote.content)
                                putExtra(EXTRA_NOTE_CREATED, lastNote.created)
                                putExtra(EXTRA_NOTE_SCROLL, lastNote.scroll)
                                putExtra(EXTRA_NOTE_CURSOR, lastNote.cursor)
                                putExtra(EXTRA_NOTE_ATTACHMENT_URI, lastNote.attachmentUri)
                                putExtra(EXTRA_NOTE_BACK_TARGET, NotesActivity::class.java.name)
                            },
                        )
                    } else {
                        context.startActivity(Intent(context, NotesActivity::class.java))
                    }
                }
            }

            QuickTile(
                modifier = Modifier.weight(1f),
                label = "Goals",
                stat = "$goalPercent% this week",
                icon = Icons.Default.Flag,
                container = ext.goals.container,
                content = ext.goals.color,
                height = height,
            ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

            QuickTile(
                modifier = Modifier.weight(1f),
                label = "Events",
                stat = when {
                    eventSummary.todayCount > 0 -> "${eventSummary.todayCount} today"
                    eventSummary.weekCount > 0 -> "${eventSummary.weekCount} this week"
                    else -> "None soon"
                },
                icon = Icons.Default.Event,
                container = ext.events.container,
                content = ext.events.color,
                height = height,
            ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

            QuickTile(
                modifier = Modifier.weight(1f),
                label = calendarLabel,
                stat = todayLabel,
                icon = Icons.Default.CalendarMonth,
                container = ext.calendar.container,
                content = ext.calendar.color,
                height = height,
            ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
        }
    }
}

@Composable
private fun QuickTile(
    label: String,
    stat: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tileScale",
    )

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .scale(scale)
            .heightIn(min = height),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stat,
                style = MaterialTheme.typography.labelSmall,
                color = content.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
