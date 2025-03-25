package com.cericatto.skillpulse.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
	@Serializable
	data object LoginScreen: Route
}