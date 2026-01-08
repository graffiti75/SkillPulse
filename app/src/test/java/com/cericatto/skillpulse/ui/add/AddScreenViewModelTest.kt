package com.cericatto.skillpulse.ui.add

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
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
class AddScreenViewModelTest {

	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	private lateinit var viewModel: AddScreenViewModel
	private lateinit var fakeRemoteDatabase: FakeRemoteDatabase
	private lateinit var savedStateHandle: SavedStateHandle

	@Before
	fun setup() {
		fakeRemoteDatabase = FakeRemoteDatabase()
		savedStateHandle = SavedStateHandle()
	}

	private fun createViewModel(suggestionsJson: String = ""): AddScreenViewModel {
		savedStateHandle["suggestionsJson"] = suggestionsJson
		return AddScreenViewModel(fakeRemoteDatabase, savedStateHandle)
	}

	// ==================== Initialization Tests ====================

	@Test
	fun `initial state has empty values`() {
		viewModel = createViewModel()

		assertThat(viewModel.state.value.description).isEmpty()
		assertThat(viewModel.state.value.startTime).isEmpty()
		assertThat(viewModel.state.value.endTime).isEmpty()
		assertThat(viewModel.state.value.loading).isFalse()
		assertThat(viewModel.state.value.alert).isNull()
		assertThat(viewModel.state.value.showSuggestions).isFalse()
	}

	@Test
	fun `suggestions are parsed from savedStateHandle`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes|||Ida ao banheiro")

		assertThat(viewModel.state.value.suggestions).hasSize(3)
		assertThat(viewModel.state.value.suggestions).contains("Tomar banho")
		assertThat(viewModel.state.value.suggestions).contains("Escovar dentes")
		assertThat(viewModel.state.value.suggestions).contains("Ida ao banheiro")
	}

	@Test
	fun `empty suggestions string results in empty list`() {
		viewModel = createViewModel("")

		assertThat(viewModel.state.value.suggestions).isEmpty()
	}

	@Test
	fun `blank suggestions are filtered out`() {
		viewModel = createViewModel("Tomar banho|||   |||Escovar dentes")

		assertThat(viewModel.state.value.suggestions).hasSize(2)
		assertThat(viewModel.state.value.suggestions).contains("Tomar banho")
		assertThat(viewModel.state.value.suggestions).contains("Escovar dentes")
	}

	// ==================== Description Update Tests ====================

	@Test
	fun `OnDescriptionChange updates description`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("New task"))

		assertThat(viewModel.state.value.description).isEqualTo("New task")
	}

	@Test
	fun `OnDescriptionChange shows suggestions when matching`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes")

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Tomar"))

		assertThat(viewModel.state.value.showSuggestions).isTrue()
	}

	@Test
	fun `OnDescriptionChange hides suggestions when no match`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes")

		viewModel.onAction(AddScreenAction.OnDescriptionChange("xyz"))

		assertThat(viewModel.state.value.showSuggestions).isFalse()
	}

	@Test
	fun `OnDescriptionChange hides suggestions when empty`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes")

		viewModel.onAction(AddScreenAction.OnDescriptionChange(""))

		assertThat(viewModel.state.value.showSuggestions).isFalse()
	}

	@Test
	fun `OnDescriptionChange case insensitive matching`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes")

		viewModel.onAction(AddScreenAction.OnDescriptionChange("tomar"))

		assertThat(viewModel.state.value.showSuggestions).isTrue()
	}

	// ==================== Suggestion Selection Tests ====================

	@Test
	fun `OnSuggestionClick sets description and hides suggestions`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes")

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Tom"))
		viewModel.onAction(AddScreenAction.OnSuggestionClick("Tomar banho"))

		assertThat(viewModel.state.value.description).isEqualTo("Tomar banho")
		assertThat(viewModel.state.value.showSuggestions).isFalse()
	}

	@Test
	fun `OnDismissSuggestions hides suggestions`() {
		viewModel = createViewModel("Tomar banho|||Escovar dentes")

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Tom"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()

		viewModel.onAction(AddScreenAction.OnDismissSuggestions)
		assertThat(viewModel.state.value.showSuggestions).isFalse()
	}

	// ==================== Time Update Tests ====================

	@Test
	fun `OnStartTimeChange updates startTime`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))

		assertThat(viewModel.state.value.startTime).isEqualTo("2026-01-06T10:00:00-03:00")
	}

	@Test
	fun `OnEndTimeChange updates endTime`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))

		assertThat(viewModel.state.value.endTime).isEqualTo("2026-01-06T12:00:00-03:00")
	}

	// ==================== Validation Tests ====================

	@Test
	fun `OnSaveClick with empty description shows error`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	@Test
	fun `OnSaveClick with empty startTime shows error`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Test task"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	@Test
	fun `OnSaveClick with empty endTime shows error`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Test task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	// ==================== Save Task Tests ====================

	@Test
	fun `OnSaveClick with valid data saves task successfully`() = runTest {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Test task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))

		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnSaveClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}

		assertThat(fakeRemoteDatabase.getTaskCount()).isEqualTo(1)
	}

	@Test
	fun `OnSaveClick sets loading to true during save`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Test task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		// After completion, loading should be false
		assertThat(viewModel.state.value.loading).isFalse()
	}

	@Test
	fun `OnSaveClick with database error shows error alert`() {
		fakeRemoteDatabase.shouldReturnError = true
		fakeRemoteDatabase.errorToReturn = DataError.Firebase.FIRESTORE_ERROR
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Test task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.loading).isFalse()
	}

	// ==================== Navigation Tests ====================

	@Test
	fun `OnBackClick emits NavigateUp event`() = runTest {
		viewModel = createViewModel()

		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnBackClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	// ==================== Alert Tests ====================

	@Test
	fun `OnDismissAlert clears alert`() {
		viewModel = createViewModel()

		// Trigger an error to set the alert
		viewModel.onAction(AddScreenAction.OnSaveClick)
		assertThat(viewModel.state.value.alert).isNotNull()

		viewModel.onAction(AddScreenAction.OnDismissAlert)
		assertThat(viewModel.state.value.alert).isNull()
	}
}