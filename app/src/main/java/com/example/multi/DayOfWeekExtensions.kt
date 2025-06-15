package com.example.multi

import java.time.DayOfWeek

internal fun DayOfWeek.toCalendarOffset(): Int = (this.value + 6) % 7
