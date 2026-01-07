package com.cericatto.skillpulse.ui.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cericatto.skillpulse.R
import com.cericatto.skillpulse.domain.errors.Result
import com.cericatto.skillpulse.domain.remote.RemoteDatabase
import com.cericatto.skillpulse.ui.MessageAlert
import com.cericatto.skillpulse.ui.UiEvent
import com.cericatto.skillpulse.ui.UiText
import com.cericatto.skillpulse.ui.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddScreenViewModel @Inject constructor(
	private val db: RemoteDatabase,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	private val _state = MutableStateFlow(AddScreenState())
	val state: StateFlow<AddScreenState> = _state.asStateFlow()

	private val _events = Channel<UiEvent>()
	val events = _events.receiveAsFlow()

	init {
		// Get suggestions from navigation arguments
		val suggestionsJson = savedStateHandle.get<String>("suggestionsJson") ?: ""
		val suggestions = parseSuggestions(suggestionsJson)

		_state.update { it.copy(suggestions = suggestions) }
		Timber.d("AddScreen initialized with ${suggestions.size} suggestions")
	}

	private fun parseSuggestions(suggestionsJson: String): List<String> {
		return if (suggestionsJson.isBlank()) {
			emptyList()
		} else {
			suggestionsJson.split("|||").filter { it.isNotBlank() }
		}
	}

	fun onAction(action: AddScreenAction) {
		when (action) {
			is AddScreenAction.OnDismissAlert -> dismissAlert()
			is AddScreenAction.OnDescriptionChange -> updateDescription(action.description)
			is AddScreenAction.OnStartTimeChange -> updateStartTime(action.startTime)
			is AddScreenAction.OnEndTimeChange -> updateEndTime(action.endTime)
			is AddScreenAction.OnSaveClick -> addTask()
			is AddScreenAction.OnBackClick -> navigateBack()
			is AddScreenAction.OnSuggestionClick -> selectSuggestion(action.suggestion)
			is AddScreenAction.OnDismissSuggestions -> dismissSuggestions()
		}
	}

	private fun updateDescription(description: String) {
		_state.update {
			it.copy(
				description = description,
				showSuggestions = description.isNotBlank() && it.suggestions.any { suggestion ->
					suggestion.contains(description, ignoreCase = true)
				}
			)
		}
	}

	private fun selectSuggestion(suggestion: String) {
		_state.update {
			it.copy(
				description = suggestion,
				showSuggestions = false
			)
		}
	}

	private fun dismissSuggestions() {
		_state.update { it.copy(showSuggestions = false) }
	}

	private fun updateStartTime(startTime: String) {
		_state.update { it.copy(startTime = startTime) }
	}

	private fun updateEndTime(endTime: String) {
		_state.update { it.copy(endTime = endTime) }
	}

	private fun addTask() {
		val currentState = _state.value

		// Validation
		if (currentState.description.isBlank()) {
			_state.update {
				it.copy(
					alert = MessageAlert(
						errorMessage = Pair(
							UiText.StringResource(R.string.validation_error),
							"Description cannot be empty"
						)
					)
				)
			}
			return
		}

		if (currentState.startTime.isBlank()) {
			_state.update {
				it.copy(
					alert = MessageAlert(
						errorMessage = Pair(
							UiText.StringResource(R.string.validation_error),
							"Start time cannot be empty"
						)
					)
				)
			}
			return
		}

		if (currentState.endTime.isBlank()) {
			_state.update {
				it.copy(
					alert = MessageAlert(
						errorMessage = Pair(
							UiText.StringResource(R.string.validation_error),
							"End time cannot be empty"
						)
					)
				)
			}
			return
		}

		viewModelScope.launch {
			_state.update { it.copy(loading = true) }

			when (val result = db.addTask(
				description = currentState.description,
				startTime = currentState.startTime,
				endTime = currentState.endTime
			)) {
				is Result.Error -> {
					Timber.e("Task couldn't be added: ${result.message}")
					_state.update {
						it.copy(
							loading = false,
							alert = MessageAlert(
								errorMessage = Pair(result.error.asUiText(), result.message ?: "")
							)
						)
					}
				}
				is Result.Success -> {
					Timber.d("Task added successfully!")
					_state.update {
						it.copy(
							loading = false,
							alert = MessageAlert(
								successMessage = UiText.StringResource(R.string.add_task_success)
							)
						)
					}
					// Navigate back after successful save
					_events.send(UiEvent.NavigateUp)
				}
			}
		}
	}

	private fun navigateBack() {
		viewModelScope.launch {
			_events.send(UiEvent.NavigateUp)
		}
	}

	private fun dismissAlert() {
		_state.update { it.copy(alert = null) }
	}
}