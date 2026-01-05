package com.cericatto.skillpulse.ui.edit

import com.cericatto.skillpulse.ui.MessageAlert

data class EditScreenState(
	val loading: Boolean = false,
	val alert: MessageAlert? = null,
	val taskId: String = "",
	val description: String = "",
	val startTime: String = "",
	val endTime: String = ""
)