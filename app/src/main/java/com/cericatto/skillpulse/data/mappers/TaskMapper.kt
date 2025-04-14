package com.cericatto.skillpulse.data.mappers

import com.cericatto.skillpulse.data.model.Task
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toTask(): Task? {
	return try {
		val id = getString("id") ?: return null
		val description = getString("description") ?: return null
		val timestamp = getLong("timestamp") ?: return null
		val time = System.currentTimeMillis()
		Task(id, description, timestamp, time, time)
	} catch (e: Exception) {
		null // Return null if mapping fails (mapNotNull will filter these out).
	}
}