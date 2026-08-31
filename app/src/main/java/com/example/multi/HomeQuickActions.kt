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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.multi.data.EventDatabase
import com.example.multi.data.toModel
import com.example.multi.ui.theme.MultiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The four shortcut tiles under the medallion. Each tile wears its segment
 * accent so the home screen reads as a colour-coded map of the app.
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar",
    height: Dp = 84.dp,
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val database = remember(appContext) { EventDatabase.getInstance(appContext) }
    val scope = rememberCoroutineScope()
    val ext = MultiTheme.extended

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = height)
            .padding(horizontal = MultiTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MultiTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuickTile(
            modifier = Modifier.weight(1f),
            label = "Notes",
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
            icon = Icons.Default.Flag,
            container = ext.goals.container,
            content = ext.goals.color,
            height = height,
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        QuickTile(
            modifier = Modifier.weight(1f),
            label = "Events",
            icon = Icons.Default.Event,
            container = ext.events.container,
            content = ext.events.color,
            height = height,
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        QuickTile(
            modifier = Modifier.weight(1f),
            label = calendarLabel,
            icon = Icons.Default.CalendarMonth,
            container = ext.calendar.container,
            content = ext.calendar.color,
            height = height,
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    }
}

@Composable
private fun QuickTile(
    label: String,
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
                .padding(vertical = MultiTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MultiTheme.spacing.xs),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
