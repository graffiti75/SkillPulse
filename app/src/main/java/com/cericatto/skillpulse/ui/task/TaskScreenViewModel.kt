package com.cericatto.skillpulse.ui.task

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TaskScreenViewModel @Inject constructor(
	private val auth: FirebaseAuth
): ViewModel() {

	private val _state = MutableStateFlow(TaskScreenState())
	val state: StateFlow<TaskScreenState> = _state.asStateFlow()

	fun onAction(action: TaskScreenAction) {
//		when (action) {
//		}
	}

	init {
		_state.update { it.copy(loading = false) }
	}
}