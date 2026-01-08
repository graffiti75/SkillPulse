package com.cericatto.skillpulse.ui.task

import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.ui.MessageAlert
import com.cericatto.skillpulse.ui.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskScreenStateTest {

	@Test
	fun `default state has correct initial values`() {
		val state = TaskScreenState()

		assertThat(state.loading).isTrue()
		assertThat(state.alert).isNull()
		assertThat(state.user).isEmpty()
		assertThat(state.tasks).isEmpty()
		assertThat(state.descriptions).isEmpty()
		assertThat(state.showDeleteDialog).isFalse()
		assertThat(state.itemToDelete).isNull()
		assertThat(state.canLoadMore).isTrue()
		assertThat(state.loadingMore).isFalse()
		assertThat(state.isFilterVisible).isFalse()
		assertThat(state.filterDate).isEmpty()
	}

	@Test
	fun `state with custom values`() {
		val tasks = listOf(
			Task(id = "1", description = "Task 1"),
			Task(id = "2", description = "Task 2")
		)
		val descriptions = setOf("Task 1", "Task 2")
		val alert = MessageAlert(successMessage = UiText.DynamicString("Success"))
		val itemToDelete = tasks.first()

		val state = TaskScreenState(
			loading = false,
			alert = alert,
			user = "test@example.com",
			tasks = tasks,
			descriptions = descriptions,
			showDeleteDialog = true,
			itemToDelete = itemToDelete,
			canLoadMore = false,
			loadingMore = true,
			isFilterVisible = true,
			filterDate = "2026-01-06"
		)

		assertThat(state.loading).isFalse()
		assertThat(state.alert).isEqualTo(alert)
		assertThat(state.user).isEqualTo("test@example.com")
		assertThat(state.tasks).isEqualTo(tasks)
		assertThat(state.descriptions).isEqualTo(descriptions)
		assertThat(state.showDeleteDialog).isTrue()
		assertThat(state.itemToDelete).isEqualTo(itemToDelete)
		assertThat(state.canLoadMore).isFalse()
		assertThat(state.loadingMore).isTrue()
		assertThat(state.isFilterVisible).isTrue()
		assertThat(state.filterDate).isEqualTo("2026-01-06")
	}

	@Test
	fun `state copy preserves unchanged values`() {
		val originalTasks = listOf(Task(id = "1", description = "Task 1"))
		val original = TaskScreenState(
			tasks = originalTasks,
			user = "original@test.com",
			filterDate = "2026-01-05"
		)

		val copied = original.copy(user = "modified@test.com")

		assertThat(copied.user).isEqualTo("modified@test.com")
		assertThat(copied.tasks).isEqualTo(originalTasks)
		assertThat(copied.filterDate).isEqualTo("2026-01-05")
	}

	@Test
	fun `state equality works correctly`() {
		val tasks = listOf(Task(id = "1", description = "Task 1"))
		val state1 = TaskScreenState(tasks = tasks, user = "test@test.com")
		val state2 = TaskScreenState(tasks = tasks, user = "test@test.com")

		assertThat(state1).isEqualTo(state2)
	}

	@Test
	fun `state inequality with different tasks`() {
		val state1 = TaskScreenState(tasks = listOf(Task(id = "1")))
		val state2 = TaskScreenState(tasks = listOf(Task(id = "2")))

		assertThat(state1).isNotEqualTo(state2)
	}

	@Test
	fun `state with empty descriptions set`() {
		val state = TaskScreenState(descriptions = emptySet())

		assertThat(state.descriptions).isEmpty()
	}

	@Test
	fun `state descriptions are unique`() {
		val descriptions = setOf("Task A", "Task B", "Task A") // Duplicate should be ignored

		val state = TaskScreenState(descriptions = descriptions)

		assertThat(state.descriptions).hasSize(2)
	}
}
