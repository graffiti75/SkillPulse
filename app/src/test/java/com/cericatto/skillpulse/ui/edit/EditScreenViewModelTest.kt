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
	fun `OnSaveClick sets loading to true during save`() = runTest {
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnSaveClick)

		// After completion, loading should be false
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `OnSaveClick with database error shows error alert`() = runTest {
		fakeRemoteDatabase.shouldReturnError = true
		fakeRemoteDatabase.errorToReturn = DataError.Firebase.FIRESTORE_ERROR
		viewModel = createViewModel()

		viewModel.onAction(EditScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `OnSaveClick preserves taskId`() = runTest {
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
	fun `OnDismissAlert clears alert`() = runTest {
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
}
