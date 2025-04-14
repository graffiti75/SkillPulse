package com.cericatto.skillpulse.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.data.model.initTaskList
import com.cericatto.skillpulse.ui.common.BottomAlert
import com.cericatto.skillpulse.ui.common.DynamicStatusBarColor
import com.cericatto.skillpulse.ui.common.shadowModifier
import com.cericatto.skillpulse.ui.common.utils.ConfirmationDialog
import com.cericatto.skillpulse.ui.common.utils.getDateTimeAsString

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
	var taskDescription by remember { mutableStateOf("") }
	Column(
		modifier = Modifier
			.background(Color.Red.copy(alpha = 0.1f))
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			text = "Welcome ${state.user}",
			style = TextStyle(
				fontSize = 20.sp
			),
			modifier = Modifier
				.padding(10.dp)
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
				onAction(TaskScreenAction.OnLoadingUpdate(true))
				onAction(TaskScreenAction.OnAddTask(description = taskDescription))
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Post Task")
		}
		Spacer(modifier = Modifier.height(16.dp))
		LazyColumn {
			items(state.tasks) { task ->
				TaskItem(
					modifier = Modifier
						.padding(top = 5.dp),
					state = state,
					task = task,
					onAction = onAction
				)
			}
		}
	}
}

@Composable
private fun TaskItem(
	modifier: Modifier = Modifier,
	state: TaskScreenState,
	task: Task,
	onAction: (TaskScreenAction) -> Unit
) {
	if (state.loading) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Transparent),
			contentAlignment = Alignment.Center
		) {
			CircularProgressIndicator(
				color = MaterialTheme.colorScheme.primary,
				strokeWidth = 4.dp,
				modifier = Modifier.size(64.dp)
			)
		}
	} else {
		Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.Start,
			modifier = modifier.shadowModifier()
				.clickable {
					onAction(TaskScreenAction.OnShowDeleteDialog(true))
				}
		) {
			StyledText(
				title = "Description",
				content = task.description
			)
			StyledText(
				title = "Start Time",
				content = task.startTime.getDateTimeAsString()
			)
			StyledText(
				title = "End Time",
				content = task.endTime.getDateTimeAsString()
			)
		}
	}
	if (state.showDeleteDialog) {
		ConfirmationDialog(
			item = task,
			onAction = onAction,
			onDeleteItem = {
				onAction(TaskScreenAction.OnDeleteTask(task))
			},
			showDialog = state.showDeleteDialog
		)
	}
}

@Composable
private fun StyledText(
	title: String,
	content: String
) {
	val annotatedString = buildAnnotatedString {
		withStyle(
			style = SpanStyle(
				fontWeight = FontWeight.Bold,
				color = Color.Black
			)
		) {
			append(title)
		}
		append(": ")
		withStyle(
			style = SpanStyle(
				fontWeight = FontWeight.Normal,
				color = Color.Black
			)
		) {
			append(content)
		}
	}

	Text(text = annotatedString)
}

@Preview
@Composable
fun TaskScreenPreview() {
	TaskScreen(
		modifier = Modifier,
		onAction = {},
		state = TaskScreenState().copy(
			tasks = initTaskList(),
			loading = false
		)
	)
}

@Preview
@Composable
fun TaskItemPreview() {
	TaskItem(
		modifier = Modifier,
		state = TaskScreenState(),
		task = Task(),
		onAction = {}
	)
}