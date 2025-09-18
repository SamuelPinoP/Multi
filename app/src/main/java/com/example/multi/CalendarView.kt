package com.example.multi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.BorderStroke
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Returns the zero-based offset used by the calendar grid for this day. */
internal fun DayOfWeek.toCalendarOffset(): Int = this.value % 7

/**
 * Simple visual calendar for the given [date]. Displays the month, year,
 * days of the week and the days of the month in a grid. No interaction
 * is provided.
 */
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun CalendarView(date: LocalDate = LocalDate.now()) {
    val yearMonth = YearMonth.from(date)
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val daysOfWeek = arrayOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )
    val locale = Locale.getDefault()

    val today = LocalDate.now()
    val headerFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", locale)
    val headerTitle = "${yearMonth.month.getDisplayName(TextStyle.FULL, locale)} ${yearMonth.year}"
    val headerSubtitle = date.format(headerFormatter)

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = headerTitle,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = headerSubtitle,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (day in daysOfWeek) {
                        val isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isWeekend) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            tonalElevation = if (isWeekend) 4.dp else 2.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = day.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isWeekend) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val firstDayOffset = firstDayOfMonth.dayOfWeek.toCalendarOffset()
                var currentDay = 1
                val totalCells = firstDayOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                if (cellIndex < firstDayOffset || currentDay > daysInMonth) {
                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    )
                                } else {
                                    val dayDate = yearMonth.atDay(currentDay)
                                    val isToday = dayDate == today
                                    val isSelectedDate = dayDate == date
                                    val isWeekend = dayDate.dayOfWeek == DayOfWeek.SATURDAY ||
                                        dayDate.dayOfWeek == DayOfWeek.SUNDAY

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        tonalElevation = when {
                                            isToday -> 8.dp
                                            isWeekend -> 3.dp
                                            else -> 1.dp
                                        },
                                        color = when {
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            isWeekend -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                            else -> MaterialTheme.colorScheme.surface
                                        },
                                        border = when {
                                            isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                            else -> null
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = currentDay.toString(),
                                                modifier = Modifier.align(Alignment.Center),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when {
                                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    isWeekend -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            if (isSelectedDate) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 4.dp)
                                                        .height(4.dp)
                                                        .fillMaxWidth(0.45f)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                            }
                                        }
                                    }
                                    currentDay++
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
