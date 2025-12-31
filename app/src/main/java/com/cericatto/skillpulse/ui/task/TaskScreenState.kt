package com.cericatto.skillpulse.ui.task

import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.ui.MessageAlert

data class TaskScreenState(
	val loading : Boolean = true,
	val alert : MessageAlert? = null,
	val user : String = "",
	val tasks : List<Task> = emptyList(),
	val showDeleteDialog : Boolean = false,
	val itemToDelete : Task? = null,
	val canLoadMore: Boolean = true,
	val loadingMore: Boolean = false,
	val isFilterVisible: Boolean = false,
	val filterDate: String = ""
)