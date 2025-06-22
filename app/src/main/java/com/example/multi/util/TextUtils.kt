package com.example.multi.util

/**
 * Capitalize the first letter of the string and any letter following a period.
 */
fun String.capitalizeAfterPeriod(): String {
    val result = StringBuilder(this.length)
    var capitalizeNext = true
    for (char in this) {
        val c = if (capitalizeNext && char.isLetter()) {
            capitalizeNext = false
            char.uppercaseChar()
        } else {
            char
        }
        result.append(c)
        if (char == '.') {
            capitalizeNext = true
        }
    }
    return result.toString()
}
