package com.example.multi.navigation

import androidx.annotation.StringRes
import com.example.multi.MedallionSegment
import com.example.multi.R

sealed class AppDestination(val route: String, @StringRes val labelRes: Int? = null) {
    data object Onboarding : AppDestination("onboarding")
    data object Lock : AppDestination("lock")
    data object Home : AppDestination("home")
    data object Notes : AppDestination("notes", R.string.segment_notes)
    data object Goals : AppDestination("goals", R.string.segment_goals)
    data object Events : AppDestination("events", R.string.segment_events)
    data object Calendar : AppDestination("calendar", R.string.segment_calendar)

    companion object {
        fun fromSegment(segment: MedallionSegment): AppDestination = when (segment) {
            MedallionSegment.NOTES -> Notes
            MedallionSegment.WEEKLY_GOALS -> Goals
            MedallionSegment.EVENTS -> Events
            MedallionSegment.CALENDAR -> Calendar
        }
    }
}
