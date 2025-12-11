package com.cericatto.skillpulse.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cericatto.skillpulse.R
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.auth.UserAuthentication
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
class TaskScreenViewModel @Inject constructor(
	private val auth: UserAuthentication,
	private val db: RemoteDatabase
): ViewModel() {

	private val _state = MutableStateFlow(TaskScreenState())
	val state: StateFlow<TaskScreenState> = _state.asStateFlow()

	private val _events = Channel<UiEvent>()
	val events = _events.receiveAsFlow()

	fun onAction(action: TaskScreenAction) {
		when (action) {
			is TaskScreenAction.OnDismissAlert -> dismissAlert()
			is TaskScreenAction.OnLoadingUpdate -> updateLoading(action.loading)
			is TaskScreenAction.OnAddTask -> addTask(action.description)
			is TaskScreenAction.OnShowDeleteDialog -> showDeleteDialog(action.show)
			is TaskScreenAction.OnDeleteTask -> deleteTask(action.task)
			is TaskScreenAction.OnLogoutClick -> logout()
		}
	}

	init {
		Timber.d("Loading tasks")
		loadTasks()
	}

	private fun updateLoading(loading: Boolean) {
		_state.update {
			it.copy(loading = loading)
		}
	}

	private fun loadTasks() {
		viewModelScope.launch {
			when (val result = db.loadTasks()) {
				is Result.Error -> {
					Timber.e("Tasks couldn't be loaded: ${result.message}")
					_state.update {
						it.copy(
							alert = MessageAlert(
								errorMessage = Pair(result.error.asUiText(), result.message ?: "")
							),
							loading = false
						)
					}
				}
				is Result.Success -> {
					Timber.d("Tasks loaded! Here they are: ${result.data}")
					_state.update {
						it.copy(
							tasks = result.data,
							loading = false
						)
					}
				}
			}
		}
	}

	private fun addTask(description: String) {
		viewModelScope.launch {
			when (val result = db.addTask(description)) {
				is Result.Error -> {
					_state.update {
						it.copy(
							alert = MessageAlert(
								errorMessage = Pair(result.error.asUiText(), result.message ?: "")
							)
						)
					}
				}
				is Result.Success -> {
					val added = result.data
					if (added) {
						_state.update {
							it.copy(
								loading = false,
								alert = MessageAlert(
									successMessage = UiText.StringResource(R.string.add_task_success)
								)
							)
						}
						loadTasks()
					}
				}
			}
		}
	}

	private fun showDeleteDialog(show: Boolean) {
		_state.update { state ->
			state.copy(
				showDeleteDialog = show
			)
		}
	}

	private fun deleteTask(task: Task) {
		val newTasks = _state.value.tasks.filter { it.id != task.id }
		_state.update { state ->
			state.copy(
				tasks = newTasks
			)
		}
	}

	private fun logout() {
		viewModelScope.launch {
			when (val result = auth.logout()) {
				is Result.Error -> {
					_state.update {
						it.copy(
							alert = MessageAlert(
								errorMessage = Pair(result.error.asUiText(), result.message ?: "")
							)
						)
					}
				}
				is Result.Success -> {
					closeCurrentScreen()
				}
			}
		}
	}

	private fun dismissAlert() {
		_state.update {
			it.copy(alert = null)
		}
	}

	private fun closeCurrentScreen() {
		viewModelScope.launch {
			dismissAlert()
			_events.send(UiEvent.NavigateUp)
		}
	}
}