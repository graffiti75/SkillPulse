package com.cericatto.skillpulse.ui.task

import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.ui.MessageAlert

data class TaskScreenState(
	val loading : Boolean = true,
	val alert : MessageAlert? = null,
	val user : String = "",
	val tasks : List<Task> = emptyList()
)