package com.cericatto.skillpulse.ui.add

sealed interface AddScreenAction {
	data object OnDismissAlert : AddScreenAction
	data class OnDescriptionChange(val description: String) : AddScreenAction
	data class OnStartTimeChange(val startTime: String) : AddScreenAction
	data class OnEndTimeChange(val endTime: String) : AddScreenAction
	data object OnSaveClick : AddScreenAction
	data object OnBackClick : AddScreenAction
}