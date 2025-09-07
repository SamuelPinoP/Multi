package com.example.multi

/** Model representing the weekly progress of a [WeeklyGoal]. */
data class WeeklyGoalRecord(
    var id: Long = 0L,
    var header: String,
    var completed: Int,
    var frequency: Int,
    var overageCount: Int = 0,
    var weekStart: String,
    var weekEnd: String,
    var dayStates: String = DEFAULT_DAY_STATES
)
