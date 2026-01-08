package com.cericatto.skillpulse.ui.add

import com.cericatto.skillpulse.ui.MessageAlert
import com.cericatto.skillpulse.ui.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AddScreenStateTest {

	@Test
	fun `default state has correct initial values`() {
		val state = AddScreenState()

		assertThat(state.loading).isFalse()
		assertThat(state.alert).isNull()
		assertThat(state.description).isEmpty()
		assertThat(state.startTime).isEmpty()
		assertThat(state.endTime).isEmpty()
		assertThat(state.suggestions).isEmpty()
		assertThat(state.showSuggestions).isFalse()
	}

	@Test
	fun `state with custom values`() {
		val alert = MessageAlert(successMessage = UiText.DynamicString("Success"))
		val suggestions = listOf("Suggestion 1", "Suggestion 2")

		val state = AddScreenState(
			loading = true,
			alert = alert,
			description = "Test description",
			startTime = "2026-01-06T10:00:00-03:00",
			endTime = "2026-01-06T12:00:00-03:00",
			suggestions = suggestions,
			showSuggestions = true
		)

		assertThat(state.loading).isTrue()
		assertThat(state.alert).isEqualTo(alert)
		assertThat(state.description).isEqualTo("Test description")
		assertThat(state.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
		assertThat(state.endTime).isEqualTo("2026-01-06T12:00:00-03:00")
		assertThat(state.suggestions).isEqualTo(suggestions)
		assertThat(state.showSuggestions).isTrue()
	}

	@Test
	fun `state copy preserves unchanged values`() {
		val original = AddScreenState(
			description = "Original",
			startTime = "2026-01-06T10:00:00-03:00"
		)

		val copied = original.copy(description = "Modified")

		assertThat(copied.description).isEqualTo("Modified")
		assertThat(copied.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
	}

	@Test
	fun `state equality works correctly`() {
		val state1 = AddScreenState(
			description = "Test",
			startTime = "2026-01-06T10:00:00-03:00"
		)
		val state2 = AddScreenState(
			description = "Test",
			startTime = "2026-01-06T10:00:00-03:00"
		)

		assertThat(state1).isEqualTo(state2)
	}

	@Test
	fun `state inequality with different values`() {
		val state1 = AddScreenState(description = "Test 1")
		val state2 = AddScreenState(description = "Test 2")

		assertThat(state1).isNotEqualTo(state2)
	}
}
