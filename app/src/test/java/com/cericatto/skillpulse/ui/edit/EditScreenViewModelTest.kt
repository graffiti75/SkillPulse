package com.cericatto.skillpulse.ui.edit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.fakes.FakeRemoteDatabase
import com.cericatto.skillpulse.fakes.MainDispatcherRule
import com.cericatto.skillpulse.ui.UiEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditScreenViewModelTest {

	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	private lateinit var viewModel: EditScreenViewModel
	private lateinit var fakeRemoteDatabase: FakeRemoteDatabase
	private lateinit var savedStateHandle: SavedStateHandle

	@Before
	fun setup() {
		fakeRemoteDatabase = FakeRemoteDatabase()
		savedStateHandle = SavedStateHandle()
	}

	private fun createViewModel(
		taskId: String = "test_task_id",
		description: String = "Test description",
		startTime: String = "2026-01-06T10:00:00-03:00",
		endTime: String = "2026-01-06T12:00:00-03:00"
	): EditScreenViewModel {
		savedStateHandle["taskId"] = taskId
		savedStateHandle["description"] = description
		savedStateHandle["startTime"] = startTime
		savedStateHandle["endTime"] = endTime

		// Add the task to fake database
		fakeRemoteDatabase.addTaskToFake(
			Task(
				id = taskId,
				description = description,
				timestamp = "2026-01-06T09:00:00-03:00",
				startTime = startTime,
				endTime = endTime
			)
		)

		return EditScreenViewModel(fakeRemoteDatabase, savedStateHandle)
	}

	// ==================== Initialization Tests ====================

	@Test
	fun `initial state is populated from savedStateHandle`() {
		viewModel = createViewModel()

		assertThat(viewModel.state.value.taskId).isEqualTo("test_task_id")
		assertThat(viewModel.state.value.description).isEqualTo("Test description")
		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-06T12:00:00-03:00")
		assertThat(viewModel.state.value.loading).isFalse()
		assertThat(viewModel.state.value.alert).isNull()
	}

	@Test
	fun `initial state with missing values uses empty strings`() {
		savedStateHandle = SavedStateHandle()
		viewModel = EditScreenViewModel(fakeRemoteDatabase, savedStateHandle)

		assertThat(viewModel.state.value.taskId).isEmpty()
		assertThat(viewModel.state.value.description).isEmpty()
		assertThat(viewModel.state.value.startTime).isEmpty()
		assertThat(viewModel.state.value.endTime).isEmpty()
	}

	// ==================== Description Update Tests ====================

	@Test
	fun `OnDescriptionChange updates description`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnDescriptionChange("Updated description"))

		assertThat(viewModel.state.value.description).isEqualTo("Updated description")
	}

	@Test
	fun `OnDescriptionChange with empty string clears description`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnDescriptionChange(""))

		assertThat(viewModel.state.value.description).isEmpty()
	}

	// ==================== Time Update Tests ====================

	@Test
	fun `OnStartTimeChange updates startTime`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnStartTimeChange("2026-01-07T14:00:00-03:00"))

		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-07T14:00:00-03:00")
	}

	@Test
	fun `OnEndTimeChange updates endTime`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnEndTimeChange("2026-01-07T16:00:00-03:00"))

		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-07T16:00:00-03:00")
	}

	// ==================== Save Task Tests ====================

	@Test
	fun `OnSaveClick updates task successfully`() = runTest {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnDescriptionChange("Updated task"))

		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnSaveClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	@Test
	fun `OnSaveClick sets loading to true during save`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnSaveClick)

		// After completion, loading should be false
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `OnSaveClick with database error shows error alert`() {
		fakeRemoteDatabase.shouldReturnError = true
		fakeRemoteDatabase.errorToReturn = DataError.Firebase.FIRESTORE_ERROR
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `OnSaveClick preserves taskId`() {
		viewModel = createViewModel(taskId = "original_task_id")

		viewModel.onAction(EditScreenAction.OnDescriptionChange("Modified"))
		viewModel.onAction(EditScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.taskId).isEqualTo("original_task_id")
	}

	// ==================== Navigation Tests ====================

	@Test
	fun `OnBackClick emits NavigateUp event`() = runTest {
		viewModel = createViewModel()

		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnBackClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	// ==================== Alert Tests ====================

	@Test
	fun `OnDismissAlert clears alert`() {
		fakeRemoteDatabase.shouldReturnError = true
		viewModel = createViewModel()

		// Trigger an error to set the alert
		viewModel.onAction(EditScreenAction.OnSaveClick)
		assertThat(viewModel.state.value.alert).isNotNull()

		viewModel.onAction(EditScreenAction.OnDismissAlert)
		assertThat(viewModel.state.value.alert).isNull()
	}

	// ==================== Multiple Update Tests ====================

	@Test
	fun `multiple field updates are preserved`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnDescriptionChange("New description"))
		viewModel.onAction(EditScreenAction.OnStartTimeChange("2026-01-08T08:00:00-03:00"))
		viewModel.onAction(EditScreenAction.OnEndTimeChange("2026-01-08T10:00:00-03:00"))

		assertThat(viewModel.state.value.description).isEqualTo("New description")
		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-08T08:00:00-03:00")
		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-08T10:00:00-03:00")
		assertThat(viewModel.state.value.taskId).isEqualTo("test_task_id")
	}

	// ==================== COMPLEX TESTS: Initialization from Navigation ====================

	@Test
	fun `state is correctly populated from navigation arguments`() {
		viewModel = createViewModel(
			taskId = "nav_task_123",
			description = "Navigation Test Task",
			startTime = "2026-01-07T08:00:00-03:00",
			endTime = "2026-01-07T17:00:00-03:00"
		)

		assertThat(viewModel.state.value.taskId).isEqualTo("nav_task_123")
		assertThat(viewModel.state.value.description).isEqualTo("Navigation Test Task")
		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-07T08:00:00-03:00")
		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-07T17:00:00-03:00")
	}

	@Test
	fun `special characters in navigation arguments are preserved`() {
		viewModel = createViewModel(
			description = "Task with special chars: @#\$%^&*()"
		)

		assertThat(viewModel.state.value.description).isEqualTo("Task with special chars: @#\$%^&*()")
	}

	// ==================== COMPLEX TESTS: Edit Operations ====================

	@Test
	fun `editing description preserves other fields`() {
		viewModel = createViewModel()
		val originalStartTime = viewModel.state.value.startTime
		val originalEndTime = viewModel.state.value.endTime
		val originalTaskId = viewModel.state.value.taskId

		viewModel.onAction(EditScreenAction.OnDescriptionChange("Modified Description"))

		assertThat(viewModel.state.value.description).isEqualTo("Modified Description")
		assertThat(viewModel.state.value.startTime).isEqualTo(originalStartTime)
		assertThat(viewModel.state.value.endTime).isEqualTo(originalEndTime)
		assertThat(viewModel.state.value.taskId).isEqualTo(originalTaskId)
	}

	@Test
	fun `editing startTime preserves other fields`() {
		viewModel = createViewModel()
		val originalDescription = viewModel.state.value.description
		val originalEndTime = viewModel.state.value.endTime

		viewModel.onAction(EditScreenAction.OnStartTimeChange("2026-01-08T09:00:00-03:00"))

		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-08T09:00:00-03:00")
		assertThat(viewModel.state.value.description).isEqualTo(originalDescription)
		assertThat(viewModel.state.value.endTime).isEqualTo(originalEndTime)
	}

	@Test
	fun `multiple edits accumulate correctly`() {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnDescriptionChange("Edit 1"))
		viewModel.onAction(EditScreenAction.OnStartTimeChange("2026-01-10T10:00:00-03:00"))
		viewModel.onAction(EditScreenAction.OnDescriptionChange("Edit 2"))
		viewModel.onAction(EditScreenAction.OnEndTimeChange("2026-01-10T18:00:00-03:00"))
		viewModel.onAction(EditScreenAction.OnDescriptionChange("Final Edit"))

		assertThat(viewModel.state.value.description).isEqualTo("Final Edit")
		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-10T10:00:00-03:00")
		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-10T18:00:00-03:00")
	}

	// ==================== COMPLEX TESTS: Save Operations ====================

	@Test
	fun `successful save updates task in database and navigates`() = runTest {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnDescriptionChange("Updated Description"))

		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnSaveClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}

		// Verify loading returned to false
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `save failure shows error and does not navigate`() {
		fakeRemoteDatabase.shouldReturnError = true
		fakeRemoteDatabase.errorToReturn = DataError.Firebase.FIRESTORE_ERROR
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `save with non-existent taskId fails`() {
		// Create task in database with different ID
		fakeRemoteDatabase.addTaskToFake(Task(id = "different_id"))

		// Create ViewModel with taskId that doesn't exist in database
		savedStateHandle["taskId"] = "non_existent_id"
		savedStateHandle["description"] = "Test"
		savedStateHandle["startTime"] = "time"
		savedStateHandle["endTime"] = "time"
		viewModel = EditScreenViewModel(fakeRemoteDatabase, savedStateHandle)

		viewModel.onAction(EditScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	@Test
	fun `can retry save after error`() = runTest {
		fakeRemoteDatabase.shouldReturnError = true
		viewModel = createViewModel()

		// First save fails
		viewModel.onAction(EditScreenAction.OnSaveClick)
		assertThat(viewModel.state.value.alert).isNotNull()

		// Dismiss error
		viewModel.onAction(EditScreenAction.OnDismissAlert)
		assertThat(viewModel.state.value.alert).isNull()

		// Fix error and retry
		fakeRemoteDatabase.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnSaveClick)
			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	// ==================== COMPLEX TESTS: taskId Immutability ====================

	@Test
	fun `taskId remains constant throughout all operations`() = runTest {
		viewModel = createViewModel(taskId = "immutable_id_123")

		val originalTaskId = viewModel.state.value.taskId

		// Perform various operations
		viewModel.onAction(EditScreenAction.OnDescriptionChange("New Description"))
		assertThat(viewModel.state.value.taskId).isEqualTo(originalTaskId)

		viewModel.onAction(EditScreenAction.OnStartTimeChange("2026-02-01T10:00:00-03:00"))
		assertThat(viewModel.state.value.taskId).isEqualTo(originalTaskId)

		viewModel.onAction(EditScreenAction.OnEndTimeChange("2026-02-01T18:00:00-03:00"))
		assertThat(viewModel.state.value.taskId).isEqualTo(originalTaskId)

		// Even after save attempt
		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnSaveClick)
			awaitItem()
		}
		assertThat(viewModel.state.value.taskId).isEqualTo(originalTaskId)
	}

	// ==================== COMPLEX TESTS: Navigation ====================

	@Test
	fun `back navigation emits NavigateUp without saving`() = runTest {
		viewModel = createViewModel()

		// Make changes
		viewModel.onAction(EditScreenAction.OnDescriptionChange("Unsaved Changes"))

		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnBackClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}

		// Changes should still be in state (not reverted)
		assertThat(viewModel.state.value.description).isEqualTo("Unsaved Changes")
	}

	// ==================== COMPLEX TESTS: Complete Edit Flow ====================

	@Test
	fun `complete edit flow - load, modify all fields, save`() = runTest {
		// Step 1: Initialize with existing task data
		viewModel = createViewModel(
			taskId = "existing_task",
			description = "Original Description",
			startTime = "2026-01-06T09:00:00-03:00",
			endTime = "2026-01-06T17:00:00-03:00"
		)

		// Verify initial state
		assertThat(viewModel.state.value.description).isEqualTo("Original Description")

		// Step 2: Modify all fields
		viewModel.onAction(EditScreenAction.OnDescriptionChange("Updated Description"))
		viewModel.onAction(EditScreenAction.OnStartTimeChange("2026-01-07T10:00:00-03:00"))
		viewModel.onAction(EditScreenAction.OnEndTimeChange("2026-01-07T18:00:00-03:00"))

		// Verify modifications
		assertThat(viewModel.state.value.description).isEqualTo("Updated Description")
		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-07T10:00:00-03:00")
		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-07T18:00:00-03:00")
		assertThat(viewModel.state.value.taskId).isEqualTo("existing_task") // Unchanged

		// Step 3: Save
		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnSaveClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}

		// Step 4: Verify state after save
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `edit flow with validation error, fix, and successful save`() = runTest {
		// Scenario: Database initially returns error, then succeeds

		fakeRemoteDatabase.shouldReturnError = true
		viewModel = createViewModel()

		// First save attempt fails
		viewModel.onAction(EditScreenAction.OnDescriptionChange("Attempt 1"))
		viewModel.onAction(EditScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()

		// User dismisses error and tries again
		viewModel.onAction(EditScreenAction.OnDismissAlert)
		fakeRemoteDatabase.shouldReturnError = false

		// Second save succeeds
		viewModel.events.test {
			viewModel.onAction(EditScreenAction.OnSaveClick)
			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}
}