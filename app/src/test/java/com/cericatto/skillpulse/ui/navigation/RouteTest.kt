package com.cericatto.skillpulse.ui.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RouteTest {

	// ==================== LoginScreen Tests ====================

	@Test
	fun `LoginScreen is a Route`() {
		val route: Route = Route.LoginScreen

		assertThat(route).isInstanceOf(Route::class.java)
		assertThat(route).isEqualTo(Route.LoginScreen)
	}

	@Test
	fun `LoginScreen singleton equality`() {
		val route1 = Route.LoginScreen
		val route2 = Route.LoginScreen

		assertThat(route1).isSameInstanceAs(route2)
	}

	// ==================== TaskScreen Tests ====================

	@Test
	fun `TaskScreen is a Route`() {
		val route: Route = Route.TaskScreen

		assertThat(route).isInstanceOf(Route::class.java)
		assertThat(route).isEqualTo(Route.TaskScreen)
	}

	@Test
	fun `TaskScreen singleton equality`() {
		val route1 = Route.TaskScreen
		val route2 = Route.TaskScreen

		assertThat(route1).isSameInstanceAs(route2)
	}

	// ==================== EditScreen Tests ====================

	@Test
	fun `EditScreen stores all parameters`() {
		val route = Route.EditScreen(
			taskId = "task_123",
			description = "Test description",
			startTime = "2026-01-06T10:00:00-03:00",
			endTime = "2026-01-06T12:00:00-03:00"
		)

		assertThat(route.taskId).isEqualTo("task_123")
		assertThat(route.description).isEqualTo("Test description")
		assertThat(route.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
		assertThat(route.endTime).isEqualTo("2026-01-06T12:00:00-03:00")
	}

	@Test
	fun `EditScreen is a Route`() {
		val route: Route = Route.EditScreen("id", "desc", "start", "end")

		assertThat(route).isInstanceOf(Route::class.java)
		assertThat(route).isInstanceOf(Route.EditScreen::class.java)
	}

	@Test
	fun `EditScreen equality works correctly`() {
		val route1 = Route.EditScreen("id", "desc", "start", "end")
		val route2 = Route.EditScreen("id", "desc", "start", "end")

		assertThat(route1).isEqualTo(route2)
	}

	@Test
	fun `EditScreen inequality with different taskId`() {
		val route1 = Route.EditScreen("id1", "desc", "start", "end")
		val route2 = Route.EditScreen("id2", "desc", "start", "end")

		assertThat(route1).isNotEqualTo(route2)
	}

	@Test
	fun `EditScreen inequality with different description`() {
		val route1 = Route.EditScreen("id", "desc1", "start", "end")
		val route2 = Route.EditScreen("id", "desc2", "start", "end")

		assertThat(route1).isNotEqualTo(route2)
	}

	@Test
	fun `EditScreen with empty strings`() {
		val route = Route.EditScreen("", "", "", "")

		assertThat(route.taskId).isEmpty()
		assertThat(route.description).isEmpty()
		assertThat(route.startTime).isEmpty()
		assertThat(route.endTime).isEmpty()
	}

	@Test
	fun `EditScreen copy creates new instance`() {
		val original = Route.EditScreen("id", "desc", "start", "end")
		val copied = original.copy(description = "modified")

		assertThat(copied.taskId).isEqualTo("id")
		assertThat(copied.description).isEqualTo("modified")
		assertThat(original.description).isEqualTo("desc")
	}

	// ==================== AddScreen Tests ====================

	@Test
	fun `AddScreen default suggestionsJson`() {
		val route = Route.AddScreen()

		assertThat(route.suggestionsJson).isEqualTo("[]")
	}

	@Test
	fun `AddScreen with custom suggestionsJson`() {
		val route = Route.AddScreen(suggestionsJson = "Task 1|||Task 2|||Task 3")

		assertThat(route.suggestionsJson).isEqualTo("Task 1|||Task 2|||Task 3")
	}

	@Test
	fun `AddScreen is a Route`() {
		val route: Route = Route.AddScreen()

		assertThat(route).isInstanceOf(Route::class.java)
		assertThat(route).isInstanceOf(Route.AddScreen::class.java)
	}

	@Test
	fun `AddScreen equality works correctly`() {
		val route1 = Route.AddScreen("suggestions")
		val route2 = Route.AddScreen("suggestions")

		assertThat(route1).isEqualTo(route2)
	}

	@Test
	fun `AddScreen inequality with different suggestions`() {
		val route1 = Route.AddScreen("suggestions1")
		val route2 = Route.AddScreen("suggestions2")

		assertThat(route1).isNotEqualTo(route2)
	}

	@Test
	fun `AddScreen with empty suggestionsJson`() {
		val route = Route.AddScreen(suggestionsJson = "")

		assertThat(route.suggestionsJson).isEmpty()
	}

	@Test
	fun `AddScreen copy creates new instance`() {
		val original = Route.AddScreen("original")
		val copied = original.copy(suggestionsJson = "modified")

		assertThat(copied.suggestionsJson).isEqualTo("modified")
		assertThat(original.suggestionsJson).isEqualTo("original")
	}

	// ==================== Type Checking Tests ====================

	@Test
	fun `can distinguish between Route types`() {
		val login: Route = Route.LoginScreen
		val task: Route = Route.TaskScreen
		val edit: Route = Route.EditScreen("id", "desc", "start", "end")
		val add: Route = Route.AddScreen()

		assertThat(login is Route.LoginScreen).isTrue()
		assertThat(task is Route.TaskScreen).isTrue()
		assertThat(edit is Route.EditScreen).isTrue()
		assertThat(add is Route.AddScreen).isTrue()

		assertThat(login is Route.TaskScreen).isFalse()
		assertThat(task is Route.LoginScreen).isFalse()
		assertThat(edit is Route.AddScreen).isFalse()
		assertThat(add is Route.EditScreen).isFalse()
	}

	@Test
	fun `when expression works with Route types`() {
		val routes = listOf(
			Route.LoginScreen,
			Route.TaskScreen,
			Route.EditScreen("id", "desc", "start", "end"),
			Route.AddScreen()
		)

		val results = routes.map { route ->
			when (route) {
				is Route.LoginScreen -> "login"
				is Route.TaskScreen -> "task"
				is Route.EditScreen -> "edit"
				is Route.AddScreen -> "add"
			}
		}

		assertThat(results).containsExactly("login", "task", "edit", "add").inOrder()
	}
}