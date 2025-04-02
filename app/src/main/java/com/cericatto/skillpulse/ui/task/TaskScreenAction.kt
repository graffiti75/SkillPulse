package com.cericatto.skillpulse.ui.task

sealed interface TaskScreenAction {
	data object OnDismissAlert : TaskScreenAction
}