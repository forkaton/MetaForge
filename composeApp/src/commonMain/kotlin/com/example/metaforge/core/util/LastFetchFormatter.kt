package com.example.metaforge.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Formats the "last successful online fetch" epoch-millis into a short label.
 * Returns "—" while a brand-new install is still waiting on its first sync.
 *
 * Example: 1748602800000 → "30 May 2026"
 */
fun formatLastFetched(epochMillis: Long?): String {
    if (epochMillis == null) return "—"
    val dt: LocalDateTime = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = MONTH_ABBR[dt.monthNumber - 1]
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    return "$day $month ${dt.year}"
}

private val MONTH_ABBR = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)
