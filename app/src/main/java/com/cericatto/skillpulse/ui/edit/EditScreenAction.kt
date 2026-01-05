package com.cericatto.skillpulse.ui.edit

sealed interface EditScreenAction {
	data object OnDismissAlert : EditScreenAction
	data class OnDescriptionChange(val description: String) : EditScreenAction
	data class OnStartTimeChange(val startTime: String) : EditScreenAction
	data class OnEndTimeChange(val endTime: String) : EditScreenAction
	data object OnSaveClick : EditScreenAction
	data object OnBackClick : EditScreenAction
	data object OnShowStartDatePicker : EditScreenAction
	data object OnShowEndDatePicker : EditScreenAction
}