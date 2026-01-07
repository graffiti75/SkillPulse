package com.cericatto.skillpulse.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TaskTest {

	@Test
	fun `Task default values are set correctly`() {
		val task = Task()
		assertThat(task.id).isNotEmpty()
		assertThat(task.description).isEqualTo("Aguar as plantas")
		assertThat(task.timestamp).isEmpty()
		assertThat(task.startTime).isEmpty()
		assertThat(task.endTime).isEmpty()
	}

	@Test
	fun `Task with custom values is created correctly`() {
		val task = Task(
			id = "test_id",
			description = "Test description",
			timestamp = "2026-01-06T10:00:00-03:00",
			startTime = "2026-01-06T09:00:00-03:00",
			endTime = "2026-01-06T11:00:00-03:00"
		)
		assertThat(task.id).isEqualTo("test_id")
		assertThat(task.description).isEqualTo("Test description")
		assertThat(task.timestamp).isEqualTo("2026-01-06T10:00:00-03:00")
		assertThat(task.startTime).isEqualTo("2026-01-06T09:00:00-03:00")
		assertThat(task.endTime).isEqualTo("2026-01-06T11:00:00-03:00")
	}

	@Test
	fun `Task copy creates new instance with modified values`() {
		val original = Task(
			id = "original_id",
			description = "Original description"
		)
		val copy = original.copy(description = "Modified description")

		assertThat(copy.id).isEqualTo("original_id")
		assertThat(copy.description).isEqualTo("Modified description")
		assertThat(original.description).isEqualTo("Original description")
	}

	@Test
	fun `Task equality works correctly`() {
		val task1 = Task(
			id = "same_id",
			description = "Same description",
			timestamp = "2026-01-06T10:00:00-03:00",
			startTime = "2026-01-06T09:00:00-03:00",
			endTime = "2026-01-06T11:00:00-03:00"
		)
		val task2 = Task(
			id = "same_id",
			description = "Same description",
			timestamp = "2026-01-06T10:00:00-03:00",
			startTime = "2026-01-06T09:00:00-03:00",
			endTime = "2026-01-06T11:00:00-03:00"
		)
		assertThat(task1).isEqualTo(task2)
	}

	@Test
	fun `Task inequality with different id`() {
		val task1 = Task(id = "id_1", description = "Same")
		val task2 = Task(id = "id_2", description = "Same")
		assertThat(task1).isNotEqualTo(task2)
	}

	@Test
	fun `initTaskList returns list of 10 tasks`() {
		val tasks = initTaskList()
		assertThat(tasks).hasSize(10)
	}

	@Test
	fun `initTaskList tasks have unique ids`() {
		val tasks = initTaskList()
		val ids = tasks.map { it.id }.toSet()
		assertThat(ids).hasSize(10)
	}

	@Test
	fun `initTaskList tasks have default description`() {
		val tasks = initTaskList()
		tasks.forEach { task ->
			assertThat(task.description).isEqualTo("Aguar as plantas")
		}
	}
}