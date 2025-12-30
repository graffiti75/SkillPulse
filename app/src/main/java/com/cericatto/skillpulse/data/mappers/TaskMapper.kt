package com.cericatto.skillpulse.data.mappers

import com.cericatto.skillpulse.data.model.Task
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toTask(): Task? {
	return try {
		val id = getString("id") ?: return null
		val description = getString("description") ?: return null
		val timestamp = getString("timestamp") ?: return null
		val startTime = getString("startTime") ?: return null
		val endTime = getString("endTime") ?: return null
		println("id: $id, description: $description, timestamp: $timestamp, startTime: $startTime, endTime: $endTime")
		Task(id, description, timestamp, startTime, endTime)
	} catch (e: Exception) {
		null // Return null if mapping fails (mapNotNull will filter these out).
	}
}