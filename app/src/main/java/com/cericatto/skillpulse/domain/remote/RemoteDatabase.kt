package com.cericatto.skillpulse.domain.remote

import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result

interface RemoteDatabase {
	suspend fun loadTasks(lastTimestamp: String?): Result<List<Task>, DataError>
	suspend fun addTask(
		description: String,
		startTime: String,
		endTime: String
	): Result<Boolean, DataError>
	suspend fun updateTask(
		taskId: String,
		description: String,
		startTime: String,
		endTime: String
	): Result<Boolean, DataError>
}