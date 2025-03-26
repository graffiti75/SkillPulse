package com.cericatto.skillpulse.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
	@Serializable
	data object LoginScreen: Route

	@Serializable
	data object TaskScreen: Route
}