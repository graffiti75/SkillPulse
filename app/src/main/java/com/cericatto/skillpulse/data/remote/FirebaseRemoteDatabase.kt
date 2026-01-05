package com.cericatto.skillpulse.data.remote

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
import java.util.UUID

class FirebaseRemoteDatabase(
	private val db: FirebaseFirestore
): RemoteDatabase {
	override suspend fun loadTasks(lastTimestamp: String?)
		: Result<List<Task>, DataError> = withContext(Dispatchers.IO) {
		try {
			// Perform a one-time fetch from Firestore
			var query = db.collection("tasks")
				.orderBy("timestamp", Query.Direction.DESCENDING)
				.limit(20)

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
		description: String
	): Result<Boolean, DataError> = withContext(Dispatchers.IO) {
		try {
			val task = hashMapOf(
				"id" to UUID.randomUUID().toString(),
				"description" to description,
				"timestamp" to System.currentTimeMillis(),
				"startTime" to System.currentTimeMillis(),
				"endTime" to System.currentTimeMillis(),
			)
			db.collection("tasks")
				.add(task)
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
}