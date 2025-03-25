package com.cericatto.skillpulse.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
	private val auth: FirebaseAuth
): ViewModel() {

	private val _state = MutableStateFlow(LoginScreenState())
	val state: StateFlow<LoginScreenState> = _state.asStateFlow()

	fun onAction(action: LoginScreenAction) {
		when (action) {
			is LoginScreenAction.OnLoginClick -> login(action.email, action.password)
			is LoginScreenAction.OnCreateUser -> createUser(action.email, action.password)
		}
	}

	init {
		_state.update { it.copy(loading = false) }
	}

	private fun login(email: String, password: String) {
		auth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					Timber.d("Login Successful!")
					// TODO: Navigate to TaskScreen (add later)
				} else {
					Timber.d("Login Failed: ${task.exception?.message}")
				}
			}
	}

	private fun createUser(email: String, password: String) {
		auth.createUserWithEmailAndPassword(email, password)
	}
}