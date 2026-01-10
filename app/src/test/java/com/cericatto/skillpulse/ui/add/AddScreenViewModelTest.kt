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

	// ==================== COMPLEX TESTS: Suggestion Matching Logic ====================

	@Test
	fun `suggestion matching works with partial word anywhere in suggestion`() {
		viewModel = createViewModel("Buy Groceries|||Weekly Team Meeting|||Call Mom")

		// Match at beginning
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Buy"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()

		// Match in middle
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Team"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()

		// Match at end
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Mom"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()
	}

	@Test
	fun `typing after selecting suggestion can show suggestions again`() {
		viewModel = createViewModel("Buy Groceries|||Buy Medicine|||Call Mom")

		// Select a suggestion
		viewModel.onAction(AddScreenAction.OnSuggestionClick("Buy Groceries"))
		assertThat(viewModel.state.value.showSuggestions).isFalse()

		// Clear and type again
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Buy"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()
	}

	@Test
	fun `suggestions with special characters are parsed correctly`() {
		viewModel = createViewModel("Task @Home|||Meeting #1|||Call & Email")

		assertThat(viewModel.state.value.suggestions).hasSize(3)
		assertThat(viewModel.state.value.suggestions).contains("Task @Home")
		assertThat(viewModel.state.value.suggestions).contains("Meeting #1")
		assertThat(viewModel.state.value.suggestions).contains("Call & Email")
	}

	@Test
	fun `empty suggestions between delimiters are filtered out`() {
		viewModel = createViewModel("Task A||||||Task B|||   |||Task C")

		assertThat(viewModel.state.value.suggestions).hasSize(3)
		assertThat(viewModel.state.value.suggestions).containsExactly("Task A", "Task B", "Task C")
	}

	// ==================== COMPLEX TESTS: Validation Flow ====================

	@Test
	fun `validation fails in order - description first`() {
		viewModel = createViewModel()

		// All fields empty - description error should show first
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("Description")
	}

	@Test
	fun `validation fails for startTime when description is filled`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Valid Task"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("Start time")
	}

	@Test
	fun `validation fails for endTime when description and startTime are filled`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Valid Task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("End time")
	}

	@Test
	fun `validation passes when all fields are filled`() = runTest {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("Valid Task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))

		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnSaveClick)

			// Should navigate up on success
			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	@Test
	fun `whitespace-only description fails validation`() {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("   "))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("Description")
	}

	// ==================== COMPLEX TESTS: Complete Save Flow ====================

	@Test
	fun `successful save adds task to database`() = runTest {
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("New Task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))

		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnSaveClick)
			awaitItem() // NavigateUp event
		}

		assertThat(fakeRemoteDatabase.getTaskCount()).isEqualTo(1)
	}

	@Test
	fun `database error during save shows error and does not navigate`() {
		fakeRemoteDatabase.shouldReturnError = true
		fakeRemoteDatabase.errorToReturn = DataError.Firebase.FIRESTORE_ERROR
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("New Task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnSaveClick)

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.loading).isFalse()
		assertThat(fakeRemoteDatabase.getTaskCount()).isEqualTo(0)
	}

	@Test
	fun `can retry save after dismissing error`() = runTest {
		fakeRemoteDatabase.shouldReturnError = true
		viewModel = createViewModel()

		viewModel.onAction(AddScreenAction.OnDescriptionChange("New Task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T12:00:00-03:00"))

		// First attempt fails
		viewModel.onAction(AddScreenAction.OnSaveClick)
		assertThat(viewModel.state.value.alert).isNotNull()

		// Dismiss error and fix the issue
		viewModel.onAction(AddScreenAction.OnDismissAlert)
		fakeRemoteDatabase.shouldReturnError = false

		// Retry should succeed
		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnSaveClick)
			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}
	}

	// ==================== COMPLEX TESTS: State Consistency ====================

	@Test
	fun `rapid description changes maintain correct suggestion state`() {
		viewModel = createViewModel("Apple|||Banana|||Cherry|||Date")

		// Rapid typing simulation
		viewModel.onAction(AddScreenAction.OnDescriptionChange("A"))
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Ap"))
		viewModel.onAction(AddScreenAction.OnDescriptionChange("App"))
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Appl"))
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Apple"))

		// Final state should show suggestions for "Apple"
		assertThat(viewModel.state.value.description).isEqualTo("Apple")
		assertThat(viewModel.state.value.showSuggestions).isTrue()
	}

	@Test
	fun `dismissing suggestions then typing shows suggestions again`() {
		viewModel = createViewModel("Task A|||Task B")

		// Type and show suggestions
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Task"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()

		// Dismiss suggestions
		viewModel.onAction(AddScreenAction.OnDismissSuggestions)
		assertThat(viewModel.state.value.showSuggestions).isFalse()

		// Type more - suggestions should reappear
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Task A"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()
	}

	@Test
	fun `all state fields are independent`() {
		viewModel = createViewModel("Suggestion")

		// Set all fields
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Description"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("Start"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("End"))

		// Verify all are set independently
		assertThat(viewModel.state.value.description).isEqualTo("Description")
		assertThat(viewModel.state.value.startTime).isEqualTo("Start")
		assertThat(viewModel.state.value.endTime).isEqualTo("End")

		// Change one shouldn't affect others
		viewModel.onAction(AddScreenAction.OnDescriptionChange("New Description"))
		assertThat(viewModel.state.value.startTime).isEqualTo("Start")
		assertThat(viewModel.state.value.endTime).isEqualTo("End")
	}

	// ==================== COMPLEX TESTS: Navigation ====================

	@Test
	fun `back navigation works even with unsaved data`() = runTest {
		viewModel = createViewModel()

		// Fill in data but don't save
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Unsaved Task"))
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))

		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnBackClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}

		// Data should still be in state (not cleared by back)
		assertThat(viewModel.state.value.description).isEqualTo("Unsaved Task")
	}

	// ==================== COMPLEX TESTS: Complete User Journey ====================

	@Test
	fun `complete flow - receive suggestions, type, select, modify, save`() = runTest {
		// Step 1: Initialize with suggestions
		viewModel = createViewModel("Buy Groceries|||Call Mom|||Weekly Report")

		assertThat(viewModel.state.value.suggestions).hasSize(3)

		// Step 2: Start typing - triggers suggestions
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Buy"))
		assertThat(viewModel.state.value.showSuggestions).isTrue()

		// Step 3: Select suggestion
		viewModel.onAction(AddScreenAction.OnSuggestionClick("Buy Groceries"))
		assertThat(viewModel.state.value.description).isEqualTo("Buy Groceries")
		assertThat(viewModel.state.value.showSuggestions).isFalse()

		// Step 4: Modify the selected suggestion
		viewModel.onAction(AddScreenAction.OnDescriptionChange("Buy Groceries - Milk, Eggs"))
		assertThat(viewModel.state.value.description).isEqualTo("Buy Groceries - Milk, Eggs")

		// Step 5: Add times
		viewModel.onAction(AddScreenAction.OnStartTimeChange("2026-01-06T10:00:00-03:00"))
		viewModel.onAction(AddScreenAction.OnEndTimeChange("2026-01-06T11:00:00-03:00"))

		// Step 6: Save
		viewModel.events.test {
			viewModel.onAction(AddScreenAction.OnSaveClick)

			val event = awaitItem()
			assertThat(event).isEqualTo(UiEvent.NavigateUp)
		}

		// Verify task was saved
		assertThat(fakeRemoteDatabase.getTaskCount()).isEqualTo(1)
	}
}