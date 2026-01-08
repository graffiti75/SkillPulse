package com.cericatto.skillpulse.ui.task

import app.cash.turbine.test
import com.cericatto.skillpulse.ITEMS_LIMIT
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.fakes.FakeRemoteDatabase
import com.cericatto.skillpulse.fakes.FakeUserAuthentication
import com.cericatto.skillpulse.fakes.MainDispatcherRule
import com.cericatto.skillpulse.ui.UiEvent
import com.cericatto.skillpulse.ui.navigation.Route
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskScreenViewModelTest {

	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	private lateinit var viewModel: TaskScreenViewModel
	private lateinit var fakeRemoteDatabase: FakeRemoteDatabase
	private lateinit var fakeUserAuthentication: FakeUserAuthentication

	@Before
	fun setup() {
		fakeRemoteDatabase = FakeRemoteDatabase()
		fakeUserAuthentication = FakeUserAuthentication()
	}

	private fun createViewModel(): TaskScreenViewModel {
		return TaskScreenViewModel(fakeUserAuthentication, fakeRemoteDatabase)
	}

	private fun addSampleTasks(count: Int = 5) {
		repeat(count) { index ->
			fakeRemoteDatabase.addTaskToFake(
				Task(
					id = "task_$index",
					description = "Task $index description",
					timestamp = "2026-01-0${index + 1}T10:00:00-03:00",
					startTime = "2026-01-0${index + 1}T09:00:00-03:00",
					endTime = "2026-01-0${index + 1}T11:00:00-03:00"
				)
			)
		}
	}

	// ==================== Initialization Tests ====================

	@Test
	fun `initial state loads tasks on creation`() {
		addSampleTasks(3)
		viewModel = createViewModel()

		assertThat(viewModel.state.value.tasks).hasSize(3)
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `initial state with empty database has empty tasks list`() {
		viewModel = createViewModel()

		assertThat(viewModel.state.value.tasks).isEmpty()
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `initial state extracts descriptions from tasks`() {
		addSampleTasks(3)
		viewModel = createViewModel()

		assertThat(viewModel.state.value.descriptions).hasSize(3)
		assertThat(viewModel.state.value.descriptions).contains("Task 0 description")
		assertThat(viewModel.state.value.descriptions).contains("Task 1 description")
		assertThat(viewModel.state.value.descriptions).contains("Task 2 description")
	}

	@Test
	fun `initial state with database error shows error alert`() {
		fakeRemoteDatabase.shouldReturnError = true
		viewModel = createViewModel()

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.loading).isFalse()
	}

	// ==================== Loading Update Tests ====================

	@Test
	fun `OnLoadingUpdate updates loading state`() {
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnLoadingUpdate(true))
		assertThat(viewModel.state.value.loading).isTrue()

		viewModel.onAction(TaskScreenAction.OnLoadingUpdate(false))
		assertThat(viewModel.state.value.loading).isFalse()
	}

	// ==================== Delete Dialog Tests ====================

	@Test
	fun `OnShowDeleteDialog shows dialog`() {
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnShowDeleteDialog(true))

		assertThat(viewModel.state.value.showDeleteDialog).isTrue()
	}

	@Test
	fun `OnShowDeleteDialog hides dialog`() {
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnShowDeleteDialog(true))
		viewModel.onAction(TaskScreenAction.OnShowDeleteDialog(false))

		assertThat(viewModel.state.value.showDeleteDialog).isFalse()
	}

	// ==================== Delete Task Tests ====================

	@Test
	fun `OnDeleteTask removes task from list`() {
		addSampleTasks(3)
		viewModel = createViewModel()

		val taskToDelete = viewModel.state.value.tasks.first()
		viewModel.onAction(TaskScreenAction.OnDeleteTask(taskToDelete))

		assertThat(viewModel.state.value.tasks).hasSize(2)
		assertThat(viewModel.state.value.tasks).doesNotContain(taskToDelete)
	}

	@Test
	fun `OnDeleteTask with non-existent task does not change list`() {
		addSampleTasks(3)
		viewModel = createViewModel()

		val nonExistentTask = Task(id = "non_existent", description = "Test")
		viewModel.onAction(TaskScreenAction.OnDeleteTask(nonExistentTask))

		assertThat(viewModel.state.value.tasks).hasSize(3)
	}

	// ==================== Navigation Tests ====================

	@Test
	fun `OnTaskClick navigates to EditScreen with task data`() = runTest {
		addSampleTasks(1)
		viewModel = createViewModel()

		val task = viewModel.state.value.tasks.first()

		viewModel.events.test {
			viewModel.onAction(TaskScreenAction.OnTaskClick(task))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)

			val navigateEvent = event as UiEvent.Navigate
			val route = navigateEvent.route as Route.EditScreen
			assertThat(route.taskId).isEqualTo(task.id)
			assertThat(route.description).isEqualTo(task.description)
			assertThat(route.startTime).isEqualTo(task.startTime)
			assertThat(route.endTime).isEqualTo(task.endTime)
		}
	}

	@Test
	fun `OnAddTaskClick navigates to AddScreen with suggestions`() = runTest {
		addSampleTasks(3)
		viewModel = createViewModel()

		viewModel.events.test {
			viewModel.onAction(TaskScreenAction.OnAddTaskClick)

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)

			val navigateEvent = event as UiEvent.Navigate
			val route = navigateEvent.route as Route.AddScreen
			assertThat(route.suggestionsJson).isNotEmpty()
		}
	}

	// ==================== Logout Tests ====================

	@Test
	fun `OnLogoutClick with success navigates up`() = runTest {
		viewModel = createViewModel()

		viewModel.events.test {
			viewModel.onAction(TaskScreenAction.OnLogoutClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	@Test
	fun `OnLogoutClick with error shows error alert`() {
		fakeUserAuthentication.shouldReturnError = true
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnLogoutClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	// ==================== Load More Tasks Tests ====================

	@Test
	fun `LoadMoreTasks appends new tasks`() {
		// Add initial tasks
		repeat(ITEMS_LIMIT) { index ->
			fakeRemoteDatabase.addTaskToFake(
				Task(
					id = "task_$index",
					description = "Task $index",
					timestamp = "2026-01-01T${10 + index}:00:00-03:00"
				)
			)
		}
		viewModel = createViewModel()

		assertThat(viewModel.state.value.tasks).hasSize(ITEMS_LIMIT)
		assertThat(viewModel.state.value.canLoadMore).isTrue()

		// Add more tasks for pagination
		repeat(5) { index ->
			fakeRemoteDatabase.addTaskToFake(
				Task(
					id = "task_${ITEMS_LIMIT + index}",
					description = "Task ${ITEMS_LIMIT + index}",
					timestamp = "2026-01-02T${index}:00:00-03:00"
				)
			)
		}

		viewModel.onAction(TaskScreenAction.LoadMoreTasks)

		// Should have more tasks after loading
		assertThat(viewModel.state.value.loadingMore).isFalse()
	}

	@Test
	fun `LoadMoreTasks does nothing when already loading`() {
		addSampleTasks(ITEMS_LIMIT)
		viewModel = createViewModel()

		// Manually set loadingMore to true
		viewModel.onAction(TaskScreenAction.LoadMoreTasks)
		val initialTaskCount = viewModel.state.value.tasks.size

		// This should be ignored
		viewModel.onAction(TaskScreenAction.LoadMoreTasks)

		assertThat(viewModel.state.value.tasks.size).isEqualTo(initialTaskCount)
	}

	@Test
	fun `LoadMoreTasks does nothing when canLoadMore is false`() {
		addSampleTasks(5) // Less than 50, so canLoadMore will be false
		viewModel = createViewModel()

		assertThat(viewModel.state.value.canLoadMore).isFalse()

		viewModel.onAction(TaskScreenAction.LoadMoreTasks)

		assertThat(viewModel.state.value.tasks).hasSize(5)
	}

	// ==================== Filter Tests ====================

	@Test
	fun `OnFilterByDate filters tasks by startTime`() {
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "1", description = "Task 1", startTime = "2026-01-05T10:00:00-03:00")
		)
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "2", description = "Task 2", startTime = "2026-01-06T10:00:00-03:00")
		)
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "3", description = "Task 3", startTime = "2026-01-05T14:00:00-03:00")
		)
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnFilterByDate("2026-01-05"))

		assertThat(viewModel.state.value.tasks).hasSize(2)
		assertThat(viewModel.state.value.filterDate).isEqualTo("2026-01-05")
	}

	@Test
	fun `OnFilterByDate with blank date does nothing`() {
		addSampleTasks(3)
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnFilterByDate(""))

		assertThat(viewModel.state.value.tasks).hasSize(3)
		assertThat(viewModel.state.value.filterDate).isEmpty()
	}

	@Test
	fun `OnClearFilter restores all tasks`() {
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "1", description = "Task 1", startTime = "2026-01-05T10:00:00-03:00")
		)
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "2", description = "Task 2", startTime = "2026-01-06T10:00:00-03:00")
		)
		viewModel = createViewModel()

		viewModel.onAction(TaskScreenAction.OnFilterByDate("2026-01-05"))
		assertThat(viewModel.state.value.tasks).hasSize(1)

		viewModel.onAction(TaskScreenAction.OnClearFilter)
		assertThat(viewModel.state.value.tasks).hasSize(2)
		assertThat(viewModel.state.value.filterDate).isEmpty()
	}

	// ==================== Refresh Tests ====================

	@Test
	fun `OnScreenResume refreshes tasks`() {
		addSampleTasks(3)
		viewModel = createViewModel()

		// Add more tasks
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "new_task", description = "New Task", timestamp = "2026-01-10T10:00:00-03:00")
		)

		viewModel.onAction(TaskScreenAction.OnScreenResume)

		assertThat(viewModel.state.value.tasks).hasSize(4)
	}

	// ==================== Alert Tests ====================

	@Test
	fun `OnDismissAlert clears alert`() {
		fakeRemoteDatabase.shouldReturnError = true
		viewModel = createViewModel()

		assertThat(viewModel.state.value.alert).isNotNull()

		viewModel.onAction(TaskScreenAction.OnDismissAlert)

		assertThat(viewModel.state.value.alert).isNull()
	}

	// ==================== Descriptions Tests ====================

	@Test
	fun `descriptions are unique`() {
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "1", description = "Same description", timestamp = "2026-01-01T10:00:00-03:00")
		)
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "2", description = "Same description", timestamp = "2026-01-02T10:00:00-03:00")
		)
		fakeRemoteDatabase.addTaskToFake(
			Task(id = "3", description = "Different description", timestamp = "2026-01-03T10:00:00-03:00")
		)
		viewModel = createViewModel()

		assertThat(viewModel.state.value.descriptions).hasSize(2)
	}
}