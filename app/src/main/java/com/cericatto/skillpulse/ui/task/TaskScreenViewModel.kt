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
import com.cericatto.skillpulse.ui.navigation.Route
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

	private var lastTimestamp: String? = null
	private var allTasks: List<Task> = emptyList()

	fun onAction(action: TaskScreenAction) {
		when (action) {
			is TaskScreenAction.OnDismissAlert -> dismissAlert()
			is TaskScreenAction.OnLoadingUpdate -> updateLoading(action.loading)
			is TaskScreenAction.OnAddTask -> addTask(action.description)
			is TaskScreenAction.OnShowDeleteDialog -> showDeleteDialog(action.show)
			is TaskScreenAction.OnDeleteTask -> deleteTask(action.task)
			is TaskScreenAction.OnLogoutClick -> logout()
			is TaskScreenAction.LoadMoreTasks -> loadMoreTasks()
			is TaskScreenAction.OnFilterByDate -> filterTasksByDate(action.date)
			is TaskScreenAction.OnClearFilter -> clearFilter()
			is TaskScreenAction.OnTaskClick -> navigateToEditScreen(action.task)
			is TaskScreenAction.OnScreenResume -> refreshTasks()
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
		_state.update { it.copy(loading = true) }
		lastTimestamp = null

		viewModelScope.launch {
			when (val result = db.loadTasks(lastTimestamp)) {
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
					val tasks = result.data
					allTasks = tasks
					lastTimestamp = tasks.lastOrNull()?.timestamp
					Timber.d("Tasks loaded! Here they are: ${tasks}")
					_state.update {
						it.copy(
							loading = false,
							tasks = tasks,
							canLoadMore = tasks.size == 20
						)
					}
				}
			}
		}
	}

	private fun loadMoreTasks() {
		if (_state.value.loadingMore || !_state.value.canLoadMore) return

		viewModelScope.launch {
			_state.update { it.copy(loadingMore = true) }

			when (val result = db.loadTasks(lastTimestamp)) {
				is Result.Success -> {
					val newTasks = result.data
					lastTimestamp = newTasks.lastOrNull()?.timestamp
					allTasks = allTasks + newTasks
					_state.update {
						it.copy(
							loadingMore = false,
							tasks = it.tasks + newTasks,
							canLoadMore = newTasks.size == 20
						)
					}
				}
				is Result.Error -> {
					_state.update {
						it.copy(
							loadingMore = false,
							alert = MessageAlert(
								errorMessage = Pair(result.error.asUiText(),
									result.message ?: "Failed to load more tasks")
							),
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

	private fun filterTasksByDate(date: String) {
		if (date.isBlank()) return

		/*
		val filteredTasks = allTasks.filter { task ->
			task.timestamp.contains(date) ||
			task.startTime.contains(date) ||
			task.endTime.contains(date)
		}
		 */
		val filteredTasks = allTasks.filter { task ->
			task.startTime.contains(date)
		}
		_state.update {
			it.copy(
				tasks = filteredTasks,
				filterDate = date,
				canLoadMore = false
			)
		}
	}

	private fun clearFilter() {
		_state.update {
			it.copy(
				tasks = allTasks,
				filterDate = "",
				canLoadMore = allTasks.size >= 20
			)
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

	private fun navigateToEditScreen(task: Task) {
		viewModelScope.launch {
			_events.send(
				UiEvent.Navigate(
					Route.EditScreen(
						taskId = task.id,
						description = task.description,
						startTime = task.startTime,
						endTime = task.endTime
					)
				)
			)
		}
	}

	private fun refreshTasks() {
		Timber.d("Refreshing tasks")
		loadTasks()
	}

	private fun closeCurrentScreen() {
		viewModelScope.launch {
			dismissAlert()
			_events.send(UiEvent.NavigateUp)
		}
	}
}