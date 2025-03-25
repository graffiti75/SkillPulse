package com.cericatto.skillpulse.ui.login

data class MessageAlert(
	val isError: Boolean = true,
	val message: String = ""
)

data class LoginScreenState(
	val loading : Boolean = true,
	val alert : MessageAlert? = null,
	val user : String = "",
	val userLogged : Boolean = false
)