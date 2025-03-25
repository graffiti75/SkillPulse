package com.cericatto.skillpulse.ui.login

sealed interface LoginScreenAction {
	data class OnLoginClick(val email: String, val password: String) : LoginScreenAction
	data class OnCreateUser(val email: String, val password: String) : LoginScreenAction
}