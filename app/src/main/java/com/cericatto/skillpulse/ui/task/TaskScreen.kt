package com.cericatto.skillpulse.ui.task

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.cericatto.skillpulse.ui.ObserveAsEvents
import com.cericatto.skillpulse.ui.UiEvent
import com.cericatto.skillpulse.ui.common.BottomAlert
import com.cericatto.skillpulse.ui.common.DynamicStatusBarColor
import com.cericatto.skillpulse.ui.common.LoadingScreen
import com.cericatto.skillpulse.ui.common.SwipeableTaskItem
import com.cericatto.skillpulse.ui.common.shadowModifier
import com.cericatto.skillpulse.ui.common.utils.ConfirmationDialog
import com.cericatto.skillpulse.ui.common.utils.formatDateString
import com.cericatto.skillpulse.ui.navigation.Route
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskScreenRoot(
	onNavigate: (Route) -> Unit,
	onNavigateUp: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: TaskScreenViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val onAction = viewModel::onAction

	ObserveAsEvents(viewModel.events) { event ->
		when (event) {
			is UiEvent.Navigate -> onNavigate(event.route)
			is UiEvent.NavigateUp -> onNavigateUp()
			else -> Unit
		}
	}

	LaunchedEffect(Unit) {
		onAction(TaskScreenAction.OnScreenResume)
	}

	DynamicStatusBarColor()
	Box(
		contentAlignment = Alignment.TopStart,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
	modifier: Modifier = Modifier,
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	onAction: (TaskScreenAction) -> Unit,
	state: TaskScreenState
) {
	val backgroundColor = if (isDarkTheme) Color.DarkGray else Color.White

	var filterDate by remember { mutableStateOf("") }
	var isFilterVisible by remember { mutableStateOf(false) }
	var showDatePicker by remember { mutableStateOf(false) }

	// Track scroll position
	val listState = rememberLazyListState()
	val currentItem by remember {
		derivedStateOf {
			listState.firstVisibleItemIndex + 1
		}
	}

	TaskScreenDatePicker(
		showDatePicker = showDatePicker,
		onDismiss = { showDatePicker = false },
		onDateSelected = { selectedDate -> filterDate = selectedDate }
	)

	Box(
		modifier = modifier
			.background(backgroundColor)
			.fillMaxSize()
	) {
		Column(
			verticalArrangement = Arrangement.Top,
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = modifier
				.background(backgroundColor)
				.fillMaxSize()
		) {
			TaskScreenTopHeader(
				user = state.user,
				taskCount = state.tasks.size,
				currentItem = currentItem,
				isFilterVisible = isFilterVisible,
				isDarkTheme = isDarkTheme,
				onFilterToggle = {
					isFilterVisible = !isFilterVisible
					if (!isFilterVisible) {
						filterDate = ""
						onAction(TaskScreenAction.OnClearFilter)
					}
				},
				onLogoutClick = { onAction(TaskScreenAction.OnLogoutClick) }
			)

			Column(
				modifier = Modifier.padding(horizontal = 16.dp)
			) {
				TaskScreenFilterTextField(
					isFilterVisible = isFilterVisible,
					filterDate = filterDate,
					onFilterDateChange = { filterDate = it },
					onDatePickerClick = { showDatePicker = true },
					onSearchClick = {
						if (filterDate.isNotBlank()) {
							onAction(TaskScreenAction.OnFilterByDate(filterDate))
						}
					},
					isDarkTheme = isDarkTheme
				)

				TaskScreenItems(
					isLoading = state.loading,
					tasks = state.tasks,
					canLoadMore = state.canLoadMore,
					loadingMore = state.loadingMore,
					listState = listState,
					isDarkTheme = isDarkTheme,
					onAction = onAction
				)
			}
		}

		// FAB to add new task
		TaskScreenFab(
			isDarkTheme = isDarkTheme,
			onClick = { onAction(TaskScreenAction.OnAddTaskClick) }
		)

		// Single confirmation dialog for the specific item being deleted
		state.itemToDelete?.let { task ->
			ConfirmationDialog(
				item = task,
				onAction = onAction
			)
		}
	}
}

@Composable
private fun BoxScope.TaskScreenFab(
	isDarkTheme: Boolean,
	onClick: () -> Unit
) {
	FloatingActionButton(
		onClick = onClick,
		modifier = Modifier
			.align(Alignment.BottomEnd)  // Now works because we're in BoxScope
			.padding(16.dp),
		containerColor = if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF1976D2)
	) {
		Icon(
			imageVector = Icons.Default.Add,
			contentDescription = "Add Task",
			tint = if (isDarkTheme) Color.Black else Color.White
		)
	}
}

@Composable
private fun TaskScreenItems(
	isLoading: Boolean,
	tasks: List<Task>,
	canLoadMore: Boolean,
	loadingMore: Boolean,
	listState: androidx.compose.foundation.lazy.LazyListState,
	isDarkTheme: Boolean,
	onAction: (TaskScreenAction) -> Unit
) {
	if (isLoading) {
		LoadingScreen()
	} else {
		LazyColumn(
			state = listState,
			verticalArrangement = Arrangement.spacedBy(5.dp),
		) {
			items(tasks) { task ->
				SwipeableTaskItem(
					item = task,
					isDarkTheme = isDarkTheme,
					onAction = onAction
				) {
					TaskItem(
						modifier = Modifier,
						task = task,
						isDarkTheme = isDarkTheme,
						onClick = { onAction(TaskScreenAction.OnTaskClick(task)) }
					)
				}
			}

			// Pagination: Load more when reaching the end
			if (canLoadMore) {
				item {
					LaunchedEffect(Unit) {
						onAction(TaskScreenAction.LoadMoreTasks)
					}
					if (loadingMore) {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.padding(16.dp),
							contentAlignment = Alignment.Center
						) {
							CircularProgressIndicator()
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreenDatePicker(
	showDatePicker: Boolean,
	onDismiss: () -> Unit,
	onDateSelected: (String) -> Unit
) {
	if (showDatePicker) {
		val datePickerState = rememberDatePickerState()

		DatePickerDialog(
			onDismissRequest = onDismiss,
			confirmButton = {
				TextButton(
					onClick = {
						val selectedDate = datePickerState.selectedDateMillis
						if (selectedDate != null) {
							val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
							val date = Instant.ofEpochMilli(selectedDate)
								.atZone(ZoneId.of("UTC"))
								.toLocalDate()
							onDateSelected(date.format(formatter))
						}
						onDismiss()
					}
				) {
					Text("OK")
				}
			},
			dismissButton = {
				TextButton(onClick = onDismiss) {
					Text("Cancel")
				}
			}
		) {
			DatePicker(state = datePickerState)
		}
	}
}

@Composable
private fun TaskScreenTopHeader(
	user: String,
	taskCount: Int,
	currentItem: Int,
	isFilterVisible: Boolean,
	isDarkTheme: Boolean,
	onFilterToggle: () -> Unit,
	onLogoutClick: () -> Unit
) {
	val textColor = if (isDarkTheme) Color.LightGray else Color.Black

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp)
	) {
		Text(
			text = "Welcome \"$user\"",
			style = TextStyle(
				fontSize = 20.sp,
				color = textColor,
				fontWeight = FontWeight.Medium
			),
			modifier = Modifier.align(Alignment.CenterStart)
		)
		Row(
			modifier = Modifier.align(Alignment.CenterEnd),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Scroll position indicator
			if (taskCount > 0) {
				Text(
					text = "$currentItem/$taskCount",
					style = TextStyle(
						fontSize = 14.sp,
						color = textColor,
						fontWeight = FontWeight.Normal
					),
					modifier = Modifier.padding(end = 8.dp)
				)
			}
			// Filter toggle icon
			IconButton(onClick = onFilterToggle) {
				Icon(
					imageVector = if (isFilterVisible) Icons.Default.FilterListOff else Icons.Default.FilterList,
					contentDescription = "Toggle Filter",
					tint = textColor
				)
			}
			// Logout icon
			IconButton(onClick = onLogoutClick) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ExitToApp,
					contentDescription = "Logout",
					tint = textColor
				)
			}
		}
	}
}

@Composable
private fun TaskScreenFilterTextField(
	isFilterVisible: Boolean,
	filterDate: String,
	onFilterDateChange: (String) -> Unit,
	onDatePickerClick: () -> Unit,
	onSearchClick: () -> Unit,
	isDarkTheme: Boolean
) {
	val textColor = if (isDarkTheme) Color.LightGray else Color.Black

	AnimatedVisibility(
		visible = isFilterVisible,
		enter = expandVertically() + fadeIn(),
		exit = shrinkVertically() + fadeOut()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(bottom = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			TextField(
				value = filterDate,
				onValueChange = onFilterDateChange,
				label = {
					Text("Filter by date (e.g., 2025-12-25)")
				},
				modifier = Modifier.weight(1f),
				singleLine = true,
				trailingIcon = {
					IconButton(onClick = onDatePickerClick) {
						Icon(
							imageVector = Icons.Default.DateRange,
							contentDescription = "Select date",
//							tint = textColor
						)
					}
				}
			)

			Spacer(modifier = Modifier.width(8.dp))

			IconButton(
				onClick = onSearchClick,
//				enabled = filterDate.isNotBlank()
			) {
				Icon(
					imageVector = Icons.Default.Search,
					contentDescription = "Search",
					tint = if (filterDate.isNotBlank()) textColor else textColor.copy(alpha = 0.4f)
				)
			}
		}
	}
}

@Composable
fun TaskItem(
	modifier: Modifier = Modifier,
	task: Task,
	isDarkTheme: Boolean,
	onClick: () -> Unit = {}
) {
	val borderColor = if (isDarkTheme) Color.DarkGray else Color.White
	Column(
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.Start,
		modifier = modifier
			.background(color = borderColor)
			.shadowModifier(outsideColor = borderColor)
			.clickable { onClick() }
	) {
		StyledText(
			title = "ID",
			content = task.id
		)
		StyledText(
			title = "Description",
			content = task.description
		)
		StyledText(
			title = "Start Time",
			content = task.startTime.formatDateString()
		)
		StyledText(
			title = "End Time",
			content = task.endTime.formatDateString()
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
			loading = true
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
			loading = true
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
		task = Task(),
		isDarkTheme = false
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
		task = Task(),
		isDarkTheme = true
	)
}