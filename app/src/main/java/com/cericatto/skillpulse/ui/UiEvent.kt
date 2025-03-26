package com.cericatto.skillpulse.ui

import com.cericatto.skillpulse.ui.navigation.Route

sealed class UiEvent {
	data class Navigate(val route: Route): UiEvent()
	data object NavigateUp: UiEvent()
}