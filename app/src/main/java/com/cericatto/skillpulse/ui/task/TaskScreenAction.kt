package com.cericatto.skillpulse.ui.task

import com.cericatto.skillpulse.data.model.Task

sealed interface TaskScreenAction {
	data object OnDismissAlert : TaskScreenAction
	data class OnLoadingUpdate(val loading: Boolean) : TaskScreenAction
	data class OnAddTask(val description: String) : TaskScreenAction
	data class OnShowDeleteDialog(val show: Boolean) : TaskScreenAction
	data class OnDeleteTask(val task: Task) : TaskScreenAction
	data object OnLogoutClick : TaskScreenAction
	data object LoadMoreTasks : TaskScreenAction
	data class OnFilterByDate(val date: String) : TaskScreenAction
	data object OnClearFilter : TaskScreenAction
	data class OnTaskClick(val task: Task) : TaskScreenAction
	data object OnScreenResume : TaskScreenAction
}