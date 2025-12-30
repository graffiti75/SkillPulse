package com.cericatto.skillpulse.ui.common.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

fun ZonedDateTime.getDateTimeAsString(pattern: String = "MMM d yyyy, HH:mm"): String {
	val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
	return this.format(formatter)
}

fun Long.toZonedDateTime(zoneId: String = ""): ZonedDateTime {
	return ZonedDateTime.ofInstant(
		Instant.ofEpochMilli(this),
		if (zoneId.isEmpty()) ZoneId.systemDefault() else ZoneId.of("UTC")
	)
}

fun Long.getDateTimeAsString(pattern: String = "MMM d yyyy, HH:mm") =
	this.toZonedDateTime().getDateTimeAsString(pattern)

fun String.formatDateString(): String {
	if (this.isNullOrBlank()) return ""
	return try {
		val inputDateTime = ZonedDateTime.parse(this)
		val formatter = DateTimeFormatter.ofPattern("MMM d yyyy, H:mm", Locale.ENGLISH)
		inputDateTime.format(formatter)
	} catch (e: DateTimeParseException) {
		""
	} catch (e: Exception) {
		""
	}
}