package com.example.multi

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Utility functions for handling event date selections and formatting.
 */
object EventDateUtils {
    private val dayNames = listOf(
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
    )

    private fun dateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    data class StoredEventDate(
        val repeatDescription: String? = null,
        val explicitDate: String? = null,
        val nextOccurrence: String? = null
    )

    /** Returns the indices of the selected days (0 = Sunday). */
    fun selectedDayIndices(dayChecks: List<Boolean>): List<Int> =
        dayChecks.mapIndexedNotNull { index, checked -> if (checked) index else null }

    /**
     * Builds a human readable repeat description when a repeat option is chosen.
     * Returns null if no repeat option is selected or no days are checked.
     */
    fun buildRepeatDescription(repeatOption: String?, dayChecks: List<Boolean>): String? {
        val indices = selectedDayIndices(dayChecks)
        if (repeatOption.isNullOrBlank() || indices.isEmpty()) {
            return null
        }
        val names = indices.map { dayNames[it] }
        val dayString = when (names.size) {
            1 -> names.first()
            2 -> "${names[0]} and ${names[1]}"
            else -> names.dropLast(1).joinToString(", ") + " and " + names.last()
        }
        return when (repeatOption) {
            "Every" -> "Every $dayString"
            "Every other" -> "Every other $dayString"
            else -> null
        }
    }

    /** Computes the next occurrence date for the selected days, if any. */
    fun computeNextOccurrence(dayChecks: List<Boolean>, base: Calendar = Calendar.getInstance()): String? {
        val indices = selectedDayIndices(dayChecks)
        return computeNextOccurrence(indices, base)
    }

    /** Computes the next occurrence date for the provided day indices. */
    fun computeNextOccurrence(indices: List<Int>, base: Calendar = Calendar.getInstance()): String? {
        if (indices.isEmpty()) return null
        val todayIndex = base.get(Calendar.DAY_OF_WEEK) - 1
        var minDiff = Int.MAX_VALUE
        indices.forEach { index ->
            var diff = (index - todayIndex + 7) % 7
            if (diff <= 0) {
                diff += 7
            }
            if (diff < minDiff) {
                minDiff = diff
            }
        }
        val target = (base.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, minDiff)
        }
        return dateFormat().format(target.time)
    }

    /** Computes the next occurrence date using a repeat description such as "Every Wednesday". */
    fun computeNextFromDescription(description: String?, base: Calendar = Calendar.getInstance()): String? {
        if (description.isNullOrBlank()) return null
        val normalized = description.lowercase(Locale.ENGLISH)
        val indices = dayNames.mapIndexedNotNull { index, name ->
            if (normalized.contains(name.lowercase(Locale.ENGLISH))) index else null
        }
        return computeNextOccurrence(indices, base)
    }

    /**
     * Formats the preview text shown to the user based on the repeat description and selected date.
     */
    fun formatPreview(selectedDate: String?, repeatDescription: String?, nextOccurrence: String?): String? {
        return when {
            !repeatDescription.isNullOrBlank() -> {
                if (!nextOccurrence.isNullOrBlank()) {
                    "$repeatDescription (Next: $nextOccurrence)"
                } else {
                    repeatDescription
                }
            }
            !selectedDate.isNullOrBlank() -> selectedDate
            else -> null
        }
    }

    /**
     * Combines the repeat description and next occurrence into a value suitable for storage.
     */
    fun buildStoredDate(repeatDescription: String?, nextOccurrence: String?, selectedDate: String?): String? {
        return when {
            !repeatDescription.isNullOrBlank() -> {
                if (!nextOccurrence.isNullOrBlank()) {
                    "$repeatDescription (Next: $nextOccurrence)"
                } else {
                    repeatDescription
                }
            }
            !selectedDate.isNullOrBlank() -> selectedDate
            else -> null
        }
    }

    /** Parses the stored date string into its components. */
    fun parseStoredDate(raw: String?): StoredEventDate {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) {
            return StoredEventDate()
        }
        return if (value.startsWith("Every", ignoreCase = true)) {
            val description = value.substringBefore("(").trim()
            val regex = Regex("Next:\\s*(\\d{4}-\\d{2}-\\d{2})")
            val match = regex.find(value)
            val next = match?.groupValues?.getOrNull(1)
            StoredEventDate(repeatDescription = description, nextOccurrence = next)
        } else {
            StoredEventDate(explicitDate = value)
        }
    }

    /** Attempts to parse a yyyy-MM-dd string into a [Calendar] instance. */
    fun parseDateToCalendar(date: String?): Calendar? {
        if (date.isNullOrBlank()) return null
        return try {
            val parsed = dateFormat().parse(date)
            if (parsed != null) {
                Calendar.getInstance().apply {
                    time = parsed
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            } else {
                null
            }
        } catch (_: ParseException) {
            null
        }
    }

    /** Resolves the next notification date based on a stored event date string. */
    fun resolveNextNotificationDate(raw: String?): String? {
        val parsed = parseStoredDate(raw)
        return when {
            !parsed.repeatDescription.isNullOrBlank() ->
                parsed.nextOccurrence ?: computeNextFromDescription(parsed.repeatDescription)
            !parsed.explicitDate.isNullOrBlank() -> parsed.explicitDate
            else -> null
        }
    }

    /** Returns the full day names list. */
    fun dayNames(): List<String> = dayNames
}
