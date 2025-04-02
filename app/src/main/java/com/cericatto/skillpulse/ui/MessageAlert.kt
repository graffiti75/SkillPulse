package com.cericatto.skillpulse.ui

data class MessageAlert(
	val errorMessage: Pair<UiText, String>? = null,
	val successMessage: UiText? = null
)