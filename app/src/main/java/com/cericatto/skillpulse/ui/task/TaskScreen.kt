package com.cericatto.skillpulse.ui.task

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import com.cericatto.skillpulse.ui.common.LoadingScreen
import com.cericatto.skillpulse.ui.common.SwipeableTaskItem
import com.cericatto.skillpulse.ui.common.shadowModifier
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
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	onAction: (TaskScreenAction) -> Unit,
	state: TaskScreenState,
	modifier: Modifier = Modifier
) {
	val backgroundColor = if (isDarkTheme) Color.DarkGray else Color.White
	val textColor = if (isDarkTheme) Color.LightGray else Color.Black

	var taskDescription by remember { mutableStateOf("") }
	Column(
		modifier = Modifier
			.background(backgroundColor)
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			text = "Welcome ${state.user}",
			style = TextStyle(
				fontSize = 20.sp,
				color = textColor
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
		LazyColumn(
			verticalArrangement = Arrangement.spacedBy(5.dp),
		) {
			items(state.tasks) { task ->
				SwipeableTaskItem(
					item = task,
					showDialog = state.showDeleteDialog,
					isDarkTheme = isDarkTheme,
					onAction = onAction
				) {
					TaskItem(
						modifier = Modifier,
						state = state,
						task = task,
						isDarkTheme = isDarkTheme,
						onAction = onAction
					)
				}
			}
		}
	}
}

@Composable
fun TaskItem(
	modifier: Modifier = Modifier,
	state: TaskScreenState,
	task: Task,
	isDarkTheme: Boolean,
	onAction: (TaskScreenAction) -> Unit
) {
	val borderColor = if (isDarkTheme) Color.DarkGray else Color.White
	if (state.loading) {
		LoadingScreen()
	} else {
		Column(
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.Start,
			modifier = modifier
				.background(color = borderColor)
				.shadowModifier(outsideColor = borderColor)
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
		withStyle(
			style = SpanStyle(
				fontWeight = FontWeight.Normal,
				color = Color.Black
			)
		) {
			append(": ")
		}
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

@Preview(
	name = "Light Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_NO,
	showBackground = true
)
@Composable
fun TaskScreenPreviewLight() {
	TaskScreen(
		isDarkTheme = false,
		modifier = Modifier,
		onAction = {},
		state = TaskScreenState().copy(
			tasks = initTaskList(),
			loading = false
		)
	)
}

@Preview(
	name = "Dark Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true
)
@Composable
fun TaskScreenPreviewDark() {
	TaskScreen(
		isDarkTheme = true,
		modifier = Modifier,
		onAction = {},
		state = TaskScreenState().copy(
			tasks = initTaskList(),
			loading = false
		)
	)
}

@Preview(
	name = "Light Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_NO,
	showBackground = true
)
@Composable
fun TaskItemPreviewLight() {
	TaskItem(
		modifier = Modifier,
		state = TaskScreenState(),
		task = Task(),
		isDarkTheme = false,
		onAction = {}
	)
}

@Preview(
	name = "Dark Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true
)
@Composable
fun TaskItemPreviewDark() {
	TaskItem(
		modifier = Modifier,
		state = TaskScreenState(),
		task = Task(),
		isDarkTheme = true,
		onAction = {}
	)
}