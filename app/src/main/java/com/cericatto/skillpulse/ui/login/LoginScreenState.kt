package com.cericatto.skillpulse.ui.login

import com.cericatto.skillpulse.ui.MessageAlert

data class LoginScreenState(
	val loading : Boolean = true,
	val alert : MessageAlert? = null,
	val user : String = "",
	val userLogged : Boolean = false
)