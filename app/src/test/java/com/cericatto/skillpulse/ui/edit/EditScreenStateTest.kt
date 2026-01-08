package com.cericatto.skillpulse.ui.edit

import com.cericatto.skillpulse.ui.MessageAlert
import com.cericatto.skillpulse.ui.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EditScreenStateTest {

	@Test
	fun `default state has correct initial values`() {
		val state = EditScreenState()

		assertThat(state.loading).isFalse()
		assertThat(state.alert).isNull()
		assertThat(state.taskId).isEmpty()
		assertThat(state.description).isEmpty()
		assertThat(state.startTime).isEmpty()
		assertThat(state.endTime).isEmpty()
	}

	@Test
	fun `state with custom values`() {
		val alert = MessageAlert(successMessage = UiText.DynamicString("Task updated"))

		val state = EditScreenState(
			loading = true,
			alert = alert,
			taskId = "task_123",
			description = "Test task description",
			startTime = "2026-01-06T10:00:00-03:00",
			endTime = "2026-01-06T12:00:00-03:00"
		)

		assertThat(state.loading).isTrue()
		assertThat(state.alert).isEqualTo(alert)
		assertThat(state.taskId).isEqualTo("task_123")
		assertThat(state.description).isEqualTo("Test task description")
		assertThat(state.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
		assertThat(state.endTime).isEqualTo("2026-01-06T12:00:00-03:00")
	}

	@Test
	fun `state copy preserves unchanged values`() {
		val original = EditScreenState(
			taskId = "original_id",
			description = "Original description",
			startTime = "2026-01-06T10:00:00-03:00"
		)

		val copied = original.copy(description = "Modified description")

		assertThat(copied.taskId).isEqualTo("original_id")
		assertThat(copied.description).isEqualTo("Modified description")
		assertThat(copied.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
	}

	@Test
	fun `state equality works correctly`() {
		val state1 = EditScreenState(
			taskId = "same_id",
			description = "Same description"
		)
		val state2 = EditScreenState(
			taskId = "same_id",
			description = "Same description"
		)

		assertThat(state1).isEqualTo(state2)
	}

	@Test
	fun `state inequality with different taskId`() {
		val state1 = EditScreenState(taskId = "id_1")
		val state2 = EditScreenState(taskId = "id_2")

		assertThat(state1).isNotEqualTo(state2)
	}

	@Test
	fun `state inequality with different description`() {
		val state1 = EditScreenState(description = "Description 1")
		val state2 = EditScreenState(description = "Description 2")

		assertThat(state1).isNotEqualTo(state2)
	}

	@Test
	fun `state with error alert`() {
		val errorAlert = MessageAlert(
			errorMessage = Pair(UiText.DynamicString("Error"), "Details")
		)

		val state = EditScreenState(alert = errorAlert)

		assertThat(state.alert).isNotNull()
		assertThat(state.alert?.errorMessage).isNotNull()
		assertThat(state.alert?.successMessage).isNull()
	}

	@Test
	fun `state with success alert`() {
		val successAlert = MessageAlert(
			successMessage = UiText.DynamicString("Success")
		)

		val state = EditScreenState(alert = successAlert)

		assertThat(state.alert).isNotNull()
		assertThat(state.alert?.successMessage).isNotNull()
		assertThat(state.alert?.errorMessage).isNull()
	}
}
