package com.cericatto.skillpulse.ui.add

import com.cericatto.skillpulse.ui.MessageAlert

data class AddScreenState(
	val loading: Boolean = false,
	val alert: MessageAlert? = null,
	val description: String = "",
	val startTime: String = "",
	val endTime: String = ""
)