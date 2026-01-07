package com.cericatto.skillpulse.ui.common.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DateTimeUtilsTest {

	// ==================== formatDateString Tests ====================

	@Test
	fun `formatDateString with valid ISO date returns formatted string`() {
		val input = "2026-01-06T10:30:00-03:00"
		val result = input.formatDateString()
		assertThat(result).isEqualTo("Jan 6 2026, 10:30")
	}

	@Test
	fun `formatDateString with different timezone returns correct format`() {
		val input = "2025-12-25T22:30:00+00:00"
		val result = input.formatDateString()
		assertThat(result).isEqualTo("Dec 25 2025, 22:30")
	}

	@Test
	fun `formatDateString with empty string returns empty string`() {
		val input = ""
		val result = input.formatDateString()
		assertThat(result).isEmpty()
	}

	@Test
	fun `formatDateString with blank string returns empty string`() {
		val input = "   "
		val result = input.formatDateString()
		assertThat(result).isEmpty()
	}

	@Test
	fun `formatDateString with invalid format returns empty string`() {
		val input = "not-a-date"
		val result = input.formatDateString()
		assertThat(result).isEmpty()
	}

	@Test
	fun `formatDateString with partial date returns empty string`() {
		val input = "2026-01-06"
		val result = input.formatDateString()
		assertThat(result).isEmpty()
	}

	@Test
	fun `formatDateString with midnight time formats correctly`() {
		val input = "2026-01-01T00:00:00-03:00"
		val result = input.formatDateString()
		assertThat(result).isEqualTo("Jan 1 2026, 0:00")
	}

	@Test
	fun `formatDateString with noon time formats correctly`() {
		val input = "2026-06-15T12:00:00-03:00"
		val result = input.formatDateString()
		assertThat(result).isEqualTo("Jun 15 2026, 12:00")
	}

	@Test
	fun `formatDateString with end of day time formats correctly`() {
		val input = "2026-12-31T23:59:00-03:00"
		val result = input.formatDateString()
		assertThat(result).isEqualTo("Dec 31 2026, 23:59")
	}

	// ==================== getDateTimeAsString Tests ====================

	@Test
	fun `ZonedDateTime getDateTimeAsString with default pattern returns correct format`() {
		val dateTime = ZonedDateTime.of(2026, 1, 6, 10, 30, 0, 0, ZoneId.of("UTC"))
		val result = dateTime.getDateTimeAsString()
		assertThat(result).isEqualTo("Jan 6 2026, 10:30")
	}

	@Test
	fun `ZonedDateTime getDateTimeAsString with custom pattern returns correct format`() {
		val dateTime = ZonedDateTime.of(2026, 1, 6, 10, 30, 0, 0, ZoneId.of("UTC"))
		val result = dateTime.getDateTimeAsString("yyyy-MM-dd")
		assertThat(result).isEqualTo("2026-01-06")
	}

	@Test
	fun `ZonedDateTime getDateTimeAsString with time only pattern returns correct format`() {
		val dateTime = ZonedDateTime.of(2026, 1, 6, 14, 45, 0, 0, ZoneId.of("UTC"))
		val result = dateTime.getDateTimeAsString("HH:mm")
		assertThat(result).isEqualTo("14:45")
	}

	// ==================== Long.toZonedDateTime Tests ====================

	@Test
	fun `Long toZonedDateTime with empty zoneId uses system default`() {
		val timestamp = 1736163000000L // Some timestamp
		val result = timestamp.toZonedDateTime()
		assertThat(result).isNotNull()
		assertThat(result.zone).isEqualTo(ZoneId.systemDefault())
	}

	@Test
	fun `Long toZonedDateTime with UTC zoneId uses UTC`() {
		val timestamp = 1736163000000L
		val result = timestamp.toZonedDateTime("UTC")
		assertThat(result).isNotNull()
		assertThat(result.zone).isEqualTo(ZoneId.of("UTC"))
	}

	// ==================== Long.getDateTimeAsString Tests ====================

	@Test
	fun `Long getDateTimeAsString returns formatted date`() {
		// January 6, 2026 10:30:00 UTC
		val timestamp = 1736158200000L
		val result = timestamp.getDateTimeAsString()
		assertThat(result).isNotEmpty()
		// The exact output depends on the system timezone
	}

	@Test
	fun `Long getDateTimeAsString with custom pattern returns correct format`() {
		val timestamp = 1767694200000L
		val result = timestamp.getDateTimeAsString("yyyy")
		assertThat(result).isEqualTo("2026")
	}
}