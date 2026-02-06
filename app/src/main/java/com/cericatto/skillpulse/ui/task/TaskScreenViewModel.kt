package com.cericatto.skillpulse.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cericatto.skillpulse.ITEMS_LIMIT
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
) : ViewModel() {

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
			is TaskScreenAction.OnShowDeleteDialog -> showDeleteDialog(action.task)
			is TaskScreenAction.OnConfirmDeleteTask -> confirmDeleteTask()
			is TaskScreenAction.OnLogoutClick -> logout()
			is TaskScreenAction.LoadMoreTasks -> loadMoreTasks()
			is TaskScreenAction.OnFilterByDate -> filterTasksByDate(action.date)
			is TaskScreenAction.OnClearFilter -> clearFilter()
			is TaskScreenAction.OnTaskClick -> navigateToEditScreen(action.task)
			is TaskScreenAction.OnScreenResume -> refreshTasks()
			is TaskScreenAction.OnAddTaskClick -> navigateToAddScreen()
		}
	}

	init {
		Timber.d("Loading tasks")
		loadTasks()
		getLoggedUser()
	}

	private fun updateLoading(loading: Boolean) {
		_state.update {
			it.copy(loading = loading)
		}
	}

	private fun getLoggedUser() {
		viewModelScope.launch {
			when (val result = auth.userLogged()) {
				is Result.Error -> {
					Timber.d("Couldn't get user name: ${result.message}")
					_state.update {
						it.copy(loading = false)
					}
				}
				is Result.Success -> {
					val userEmail = result.data
					if (userEmail.isNotBlank()) {
						Timber.d("User $userEmail logged in now")
						_state.update {
							it.copy(
								user = userEmail.split("@")[0],
								loading = false
							)
						}
					} else {
						Timber.d("No user logged in")
						_state.update {
							it.copy(loading = false)
						}
					}
				}
			}
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

					// Extract unique descriptions for suggestions
					val descriptions = tasks.map { it.description }.toSet()

					Timber.d("Tasks loaded! Here they are: ${tasks}")
					_state.update {
						it.copy(
							loading = false,
							tasks = tasks,
							descriptions = descriptions,
							canLoadMore = tasks.size == ITEMS_LIMIT
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
					// Add new descriptions to the existing set
					val newDescriptions = newTasks.map { it.description }.toSet()
					_state.update {
						it.copy(
							loadingMore = false,
							tasks = it.tasks + newTasks,
							descriptions = it.descriptions + newDescriptions,
							canLoadMore = newTasks.size == ITEMS_LIMIT
						)
					}
				}

				is Result.Error -> {
					_state.update {
						it.copy(
							loadingMore = false,
							alert = MessageAlert(
								errorMessage = Pair(
									result.error.asUiText(),
									result.message ?: "Failed to load more tasks"
								)
							),
						)
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
				canLoadMore = allTasks.size >= ITEMS_LIMIT
			)
		}
	}

	private fun confirmDeleteTask() {
		val task = _state.value.itemToDelete ?: return
		deleteTask(task)
	}

	private fun showDeleteDialog(task: Task?) {
		_state.update { state ->
			state.copy(
				showDeleteDialog = task != null,
				itemToDelete = task
			)
		}
	}

	private fun deleteTask(task: Task) {
		viewModelScope.launch {
			_state.update { it.copy(showDeleteDialog = false, itemToDelete = null) }

			when (val result = db.deleteTask(task.id)) {
				is Result.Error -> {
					Timber.e("Task deletion failed: ${result.message}")
					_state.update {
						it.copy(
							alert = MessageAlert(
								errorMessage = Pair(
									result.error.asUiText(),
									result.message ?: "Failed to delete task"
								)
							)
						)
					}
				}

				is Result.Success -> {
					Timber.d("Task deleted successfully: ${task.id}")
					allTasks = allTasks.filter { it.id != task.id }
					val newTasks = _state.value.tasks.filter { it.id != task.id }
					_state.update { state ->
						state.copy(
							tasks = newTasks,
							alert = MessageAlert(
								successMessage = UiText.StringResource(R.string.delete_task_success)
							)
						)
					}
				}
			}
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

	private fun navigateToAddScreen() {
		viewModelScope.launch {
			// Convert descriptions to JSON string for navigation
			val suggestionsJson = _state.value.descriptions.joinToString(
				separator = "|||"
			)
			_events.send(
				UiEvent.Navigate(
					Route.AddScreen(
						suggestionsJson = suggestionsJson
					)
				)
			)
		}
	}

	private fun closeCurrentScreen() {
		viewModelScope.launch {
			dismissAlert()
			_events.send(UiEvent.NavigateUp)
		}
	}
}