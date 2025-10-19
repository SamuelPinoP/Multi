package com.example.multi

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.multi.ui.components.ModernActionButton
import com.example.multi.ui.components.ModernButtonDefaults

/**
 * A single-row bar of 4 luxe-looking quick-action buttons:
 * Notes • Goals • Events • Calendar (or Dates)
 *
 * Place it under the medallion on the Home screen.
 */
@Composable
fun HomeQuickActions(
    modifier: Modifier = Modifier,
    calendarLabel: String = "Calendar"
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModernActionButton(
            modifier = Modifier
                .weight(1f)
                .height(108.dp),
            label = "Notes",
            colors = ModernButtonDefaults.notes(),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        ) { context.startActivity(Intent(context, NotesActivity::class.java)) }

        ModernActionButton(
            modifier = Modifier
                .weight(1f)
                .height(108.dp),
            label = "Goals",
            colors = ModernButtonDefaults.goals(),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        ) { context.startActivity(Intent(context, WeeklyGoalsActivity::class.java)) }

        ModernActionButton(
            modifier = Modifier
                .weight(1f)
                .height(108.dp),
            label = "Events",
            colors = ModernButtonDefaults.events(),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        ) { context.startActivity(Intent(context, EventsActivity::class.java)) }

        ModernActionButton(
            modifier = Modifier
                .weight(1f)
                .height(108.dp),
            label = calendarLabel,
            colors = ModernButtonDefaults.calendar(),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
        ) { context.startActivity(Intent(context, CalendarMenuActivity::class.java)) }
    }
}