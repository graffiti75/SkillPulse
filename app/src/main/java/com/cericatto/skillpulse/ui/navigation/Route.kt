package com.cericatto.skillpulse.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
	@Serializable
	data object LoginScreen: Route

	@Serializable
	data object TaskScreen: Route

	@Serializable
	data class EditScreen(
		val taskId: String,
		val description: String,
		val startTime: String,
		val endTime: String
	): Route

	@Serializable
	data class AddScreen(
		val suggestionsJson: String = "[]"
	): Route
}