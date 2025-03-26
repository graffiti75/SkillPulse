package com.cericatto.skillpulse.ui.login

import com.cericatto.skillpulse.ui.UiText

data class MessageAlert(
	val errorMessage: Pair<UiText, String>? = null,
	val successMessage: UiText? = null
)

data class LoginScreenState(
	val loading : Boolean = true,
	val alert : MessageAlert? = null,
	val user : String = "",
	val userLogged : Boolean = false
)