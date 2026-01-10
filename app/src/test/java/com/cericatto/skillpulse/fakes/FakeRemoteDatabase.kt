package com.cericatto.skillpulse.fakes

import com.cericatto.skillpulse.ITEMS_LIMIT
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result
import com.cericatto.skillpulse.domain.remote.RemoteDatabase

class FakeRemoteDatabase : RemoteDatabase {

	private val tasks = mutableListOf<Task>()
	var shouldReturnError = false
	var errorToReturn: DataError = DataError.Firebase.FIRESTORE_ERROR

	fun addTaskToFake(task: Task) {
		tasks.add(task)
	}

	fun clearTasks() {
		tasks.clear()
	}

	fun getTaskCount() = tasks.size

	override suspend fun loadTasks(lastTimestamp: String?): Result<List<Task>, DataError> {
		return if (shouldReturnError) {
			Result.Error(errorToReturn, "Fake error")
		} else {
			val sortedTasks = tasks.sortedByDescending { it.timestamp }
			val startIndex = if (lastTimestamp != null) {
				sortedTasks.indexOfFirst { it.timestamp == lastTimestamp } + 1
			} else {
				0
			}
			val endIndex = minOf(startIndex + ITEMS_LIMIT, sortedTasks.size)
			Result.Success(sortedTasks.subList(startIndex, endIndex))
		}
	}

	override suspend fun addTask(
		description: String,
		startTime: String,
		endTime: String
	): Result<Boolean, DataError> {
		return if (shouldReturnError) {
			Result.Error(errorToReturn, "Fake error")
		} else {
			val task = Task(
				id = "fake_${tasks.size + 1}",
				description = description,
				timestamp = System.currentTimeMillis().toString(),
				startTime = startTime,
				endTime = endTime
			)
			tasks.add(task)
			Result.Success(true)
		}
	}

	override suspend fun updateTask(
		taskId: String,
		description: String,
		startTime: String,
		endTime: String
	): Result<Boolean, DataError> {
		return if (shouldReturnError) {
			Result.Error(errorToReturn, "Fake error")
		} else {
			val index = tasks.indexOfFirst { it.id == taskId }
			if (index == -1) {
				Result.Error(DataError.Firebase.FIRESTORE_ERROR, "Task not found")
			} else {
				tasks[index] = tasks[index].copy(
					description = description,
					startTime = startTime,
					endTime = endTime
				)
				Result.Success(true)
			}
		}
	}
}
