package com.cericatto.skillpulse.data.remote

import com.cericatto.skillpulse.ITEMS_LIMIT
import com.cericatto.skillpulse.data.mappers.toTask
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result
import com.cericatto.skillpulse.domain.remote.RemoteDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class FirebaseRemoteDatabase(
	private val db: FirebaseFirestore
): RemoteDatabase {
	override suspend fun loadTasks(lastTimestamp: String?)
		: Result<List<Task>, DataError> = withContext(Dispatchers.IO) {
		try {
			// Perform a one-time fetch from Firestore
			var query = db.collection("tasks")
				.orderBy("timestamp", Query.Direction.DESCENDING)
				.limit(ITEMS_LIMIT)

			// If we have a lastTimestamp, start after it for pagination
			if (lastTimestamp != null) {
				query = query.startAfter(lastTimestamp)
			}

			val snapshot = query.get().await()
			val tasks = snapshot.documents.mapNotNull { it.toTask() }
			Result.Success(tasks)
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.FIRESTORE_ERROR,
				message = e.message ?: "Failed to load tasks"
			)
		}
	}

	override suspend fun addTask(
		description: String,
		startTime: String,
		endTime: String
	): Result<Boolean, DataError> = withContext(Dispatchers.IO) {
		try {
			// Generate ID in format YYYYMMDD_N
			val taskId = generateTaskId(startTime)

			// Use ISO format for timestamp to match existing data format
			val timestamp = java.time.ZonedDateTime.now()
				.truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

			val task = hashMapOf(
				"id" to taskId,
				"description" to description,
				"timestamp" to timestamp,
				"startTime" to startTime,
				"endTime" to endTime,
			)
			db.collection("tasks")
				.document(taskId)
				.set(task)
				.await() // Suspends until the write completes
			Result.Success(true)
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.FIRESTORE_ERROR,
				message = e.message ?: "Failed to add task"
			)
		}
	}

	override suspend fun updateTask(
		taskId: String,
		description: String,
		startTime: String,
		endTime: String
	): Result<Boolean, DataError> = withContext(Dispatchers.IO) {
		try {
			// Find the document with the matching taskId
			val querySnapshot = db.collection("tasks")
				.whereEqualTo("id", taskId)
				.get()
				.await()

			if (querySnapshot.documents.isEmpty()) {
				return@withContext Result.Error(
					error = DataError.Firebase.FIRESTORE_ERROR,
					message = "Task not found"
				)
			}

			// Update the first matching document
			val documentId = querySnapshot.documents.first().id
			val updates = hashMapOf<String, Any>(
				"description" to description,
				"startTime" to startTime,
				"endTime" to endTime
			)

			db.collection("tasks")
				.document(documentId)
				.update(updates)
				.await()

			Result.Success(true)
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.FIRESTORE_ERROR,
				message = e.message ?: "Failed to update task"
			)
		}
	}

	private suspend fun generateTaskId(startTime: String): String {
		// Parse the startTime (ISO 8601 format: 2026-01-02T14:00:00-03:00)
		val zonedDateTime = java.time.ZonedDateTime.parse(startTime)
		val datePrefix = zonedDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

		// Query all tasks with IDs starting with today's date prefix
		val snapshot = db.collection("tasks")
			.orderBy("id", Query.Direction.DESCENDING)
			.get()
			.await()

		// Find the highest number for today's date
		var maxNumber = 0
		for (document in snapshot.documents) {
			val id = document.getString("id") ?: continue
			if (id.startsWith(datePrefix + "_")) {
				val numberPart = id.substringAfter("_").toIntOrNull() ?: 0
				if (numberPart > maxNumber) {
					maxNumber = numberPart
				}
			}
		}

		// Return the next ID
		return "${datePrefix}_${maxNumber + 1}"
	}
}