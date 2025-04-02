package com.cericatto.skillpulse.ui.task

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cericatto.skillpulse.data.mappers.toTask
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.ui.common.BottomAlert
import com.cericatto.skillpulse.ui.common.DynamicStatusBarColor
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TaskScreenRoot(
	modifier: Modifier = Modifier,
	viewModel: TaskScreenViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val onAction = viewModel::onAction

	DynamicStatusBarColor()
	Box(
		contentAlignment = Alignment.BottomCenter,
		modifier = Modifier.fillMaxSize()
	) {
		TaskScreen(
			modifier = modifier,
			onAction = onAction,
			state = state
		)
		state.alert?.let {
			BottomAlert(
				onDismiss = { onAction(TaskScreenAction.OnDismissAlert) },
				alert = it
			)
		}
	}
}

@Composable
fun TaskScreen(
	modifier: Modifier = Modifier,
	onAction: (TaskScreenAction) -> Unit,
	state: TaskScreenState
) {
	val db = FirebaseFirestore.getInstance()
	var taskDescription by remember { mutableStateOf("") }
	var tasks by remember { mutableStateOf(listOf<Task>()) }

	// Load tasks from Firestore.
	LaunchedEffect(Unit) {
		db.collection("tasks")
			.addSnapshotListener { snapshot, e ->
				if (e != null) return@addSnapshotListener
				tasks = snapshot?.documents?.map {
					it.toTask() ?: Task("", 0)
				} ?: emptyList()
			}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			text = "Welcome ${state.user}",
			style = TextStyle(
				fontSize = 20.sp
			),
			modifier = Modifier.padding(10.dp)
				.padding(bottom = 20.dp)
		)

		TextField(
			value = taskDescription,
			onValueChange = {
				taskDescription = it
							},
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
				Text(
					text = task.toString(),
					modifier = Modifier.padding(8.dp)
				)
			}
		}
	}
}

@Preview
@Composable
fun TaskScreenPreview() {
	TaskScreen(
		modifier = Modifier,
		onAction = {},
		state = TaskScreenState()
	)
}