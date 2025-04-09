package com.cericatto.skillpulse.data.remote

import com.cericatto.skillpulse.data.mappers.toTask
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result
import com.cericatto.skillpulse.domain.remote.RemoteDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseRemoteDatabase(
	private val db: FirebaseFirestore
): RemoteDatabase {
	override suspend fun loadTasks(): Result<List<Task>, DataError> = withContext(Dispatchers.IO) {
		try {
			// Perform a one-time fetch from Firestore
			val snapshot = db.collection("tasks")
				.get()
				.await() // Suspends until the query completes.

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
}