package com.cericatto.skillpulse.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cericatto.skillpulse.R
import com.cericatto.skillpulse.domain.auth.UserAuthentication
import com.cericatto.skillpulse.domain.errors.Result
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
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
	private val auth: UserAuthentication
): ViewModel() {

	private val _state = MutableStateFlow(LoginScreenState())
	val state: StateFlow<LoginScreenState> = _state.asStateFlow()

	private val _events = Channel<UiEvent>()
	val events = _events.receiveAsFlow()

	fun onAction(action: LoginScreenAction) {
		when (action) {
			is LoginScreenAction.OnLoginClick -> login(action.email, action.password)
			is LoginScreenAction.OnCreateUser -> createUser(action.email, action.password)
			is LoginScreenAction.OnDismissAlert -> dismissAlert()
		}
	}

	init {
		_state.update { it.copy(loading = false) }
	}

	private fun login(email: String, password: String) {
		viewModelScope.launch {
			when (val result = auth.login(email, password)) {
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
					goToTaskScreen()
				}
			}
		}
	}

	private fun createUser(email: String, password: String) {
		viewModelScope.launch {
			when (val result = auth.signUp(email, password)) {
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
					_state.update {
						it.copy(
							alert = MessageAlert(
								successMessage = UiText.StringResource(R.string.signup_success)
							)
						)
					}
				}
			}
		}
	}

	private fun dismissAlert() {
		_state.update {
			it.copy(alert = null)
		}
	}

	private fun goToTaskScreen() {
		viewModelScope.launch {
			dismissAlert()
			_events.send(UiEvent.Navigate(Route.TaskScreen))
		}
	}
}