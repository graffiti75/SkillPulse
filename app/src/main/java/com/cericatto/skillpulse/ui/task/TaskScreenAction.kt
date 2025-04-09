package com.cericatto.skillpulse.ui.task

sealed interface TaskScreenAction {
	data object OnDismissAlert : TaskScreenAction
	data class OnLoadingUpdate(val loading: Boolean) : TaskScreenAction
	data class OnAddTask(val description: String) : TaskScreenAction
}