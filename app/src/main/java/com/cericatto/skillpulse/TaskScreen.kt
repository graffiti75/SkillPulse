package com.cericatto.skillpulse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SkillPulseTaskScreen() {
	val db = FirebaseFirestore.getInstance()
	var taskDescription by remember { mutableStateOf("") }
	var tasks by remember { mutableStateOf(listOf<String>()) }

	// Load tasks from Firestore
	LaunchedEffect(Unit) {
		db.collection("tasks")
			.addSnapshotListener { snapshot, e ->
				if (e != null) return@addSnapshotListener
				tasks = snapshot?.documents?.map { it.getString("description") ?: "" } ?: emptyList()
			}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		TextField(
			value = taskDescription,
			onValueChange = { taskDescription = it },
			label = {
				Text("Enter a task (e.g., Fix my code)")
			},
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(8.dp))
		Button(
			onClick = {
				if (taskDescription.isNotEmpty()) {
					val task = hashMapOf(
						"description" to taskDescription,
						"timestamp" to System.currentTimeMillis()
					)
					db.collection("tasks").add(task)
					taskDescription = ""
				}
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Post Task")
		}
		Spacer(modifier = Modifier.height(16.dp))
		LazyColumn {
			items(tasks) { task ->
				Text(text = task, modifier = Modifier.padding(8.dp))
			}
		}
	}
}

@Preview
@Composable
fun SkillPulseTaskScreenPreview() {
	SkillPulseTaskScreen()
}