package com.example.metaforge.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [formatLastFetched].
 *
 * Date assertions stay timezone-independent: we pick a mid-month, mid-day UTC
 * instant so even ±14h offsets can't shift the day/month/year out from under
 * the test on CI runners in unexpected zones.
 */
class LastFetchFormatterTest {

    @Test
    fun `null epoch returns the empty placeholder`() {
        assertEquals("—", formatLastFetched(null))
    }

    @Test
    fun `mid-month UTC instant survives any reasonable timezone shift`() {
        // 2024-06-15T12:00:00Z. Even at UTC-12 the day is still 15 Jun 2024
        // and at UTC+14 it bumps to 16 Jun — month and year stay put.
        val midJune2024 = 1718452800000L

        val out = formatLastFetched(midJune2024)

        assertTrue(out.contains("Jun"), "month token must be 'Jun' (got $out)")
        assertTrue(out.contains("2024"), "year must be 2024 (got $out)")
        // "DD Mon YYYY" — exactly three space-separated tokens.
        assertEquals(3, out.split(" ").size, "expected 'DD MMM YYYY' shape, got $out")
    }

    @Test
    fun `day component is zero-padded`() {
        // 2024-01-05T12:00:00Z → "05 Jan 2024" in most zones.
        val fifthJan = 1704456000000L
        val out = formatLastFetched(fifthJan)
        // Day token is the first whitespace chunk; must be exactly two chars.
        assertEquals(2, out.split(" ").first().length,
            "day must be zero-padded to two digits (got '$out')")
    }
}
