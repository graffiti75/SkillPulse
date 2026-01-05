package com.cericatto.skillpulse.ui.edit

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
class EditScreenViewModel @Inject constructor(
	private val db: RemoteDatabase,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	private val _state = MutableStateFlow(EditScreenState())
	val state: StateFlow<EditScreenState> = _state.asStateFlow()

	private val _events = Channel<UiEvent>()
	val events = _events.receiveAsFlow()

	init {
		val taskId = savedStateHandle.get<String>("taskId") ?: ""
		val description = savedStateHandle.get<String>("description") ?: ""
		val startTime = savedStateHandle.get<String>("startTime") ?: ""
		val endTime = savedStateHandle.get<String>("endTime") ?: ""

		_state.update {
			it.copy(
				taskId = taskId,
				description = description,
				startTime = startTime,
				endTime = endTime
			)
		}
		Timber.d("EditScreen initialized with taskId: $taskId")
	}

	fun onAction(action: EditScreenAction) {
		when (action) {
			is EditScreenAction.OnDismissAlert -> dismissAlert()
			is EditScreenAction.OnDescriptionChange -> updateDescription(action.description)
			is EditScreenAction.OnStartTimeChange -> updateStartTime(action.startTime)
			is EditScreenAction.OnEndTimeChange -> updateEndTime(action.endTime)
			is EditScreenAction.OnSaveClick -> saveTask()
			is EditScreenAction.OnBackClick -> navigateBack()
			is EditScreenAction.OnShowStartDatePicker -> { /* Handled in UI */ }
			is EditScreenAction.OnShowEndDatePicker -> { /* Handled in UI */ }
		}
	}

	private fun updateDescription(description: String) {
		_state.update { it.copy(description = description) }
	}

	private fun updateStartTime(startTime: String) {
		_state.update { it.copy(startTime = startTime) }
	}

	private fun updateEndTime(endTime: String) {
		_state.update { it.copy(endTime = endTime) }
	}

	private fun saveTask() {
		viewModelScope.launch {
			_state.update { it.copy(loading = true) }

			when (val result = db.updateTask(
				taskId = _state.value.taskId,
				description = _state.value.description,
				startTime = _state.value.startTime,
				endTime = _state.value.endTime
			)) {
				is Result.Error -> {
					Timber.e("Task couldn't be updated: ${result.message}")
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
					Timber.d("Task updated successfully!")
					_state.update {
						it.copy(
							loading = false,
							alert = MessageAlert(
								successMessage = UiText.StringResource(R.string.update_task_success)
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