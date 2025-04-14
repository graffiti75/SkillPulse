package com.cericatto.skillpulse.ui.task

import com.cericatto.skillpulse.data.model.Task

sealed interface TaskScreenAction {
	data object OnDismissAlert : TaskScreenAction
	data class OnLoadingUpdate(val loading: Boolean) : TaskScreenAction
	data class OnAddTask(val description: String) : TaskScreenAction
	data class OnShowDeleteDialog(val show: Boolean) : TaskScreenAction
	data class OnDeleteTask(val task: Task) : TaskScreenAction
}