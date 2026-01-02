package com.cericatto.skillpulse.ui.task

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.filled.DateRange
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.cericatto.skillpulse.ui.common.utils.formatDateString
import com.cericatto.skillpulse.ui.navigation.Route

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
	val textColor = if (isDarkTheme) Color.LightGray else Color.Black

	var filterDate by remember { mutableStateOf("") }
	var isFilterVisible by remember { mutableStateOf(false) }
	var showDatePicker by remember { mutableStateOf(false) }

	// DatePicker state
	val datePickerState = rememberDatePickerState()

	// Track scroll position
	val listState = rememberLazyListState()
	val currentItem by remember {
		derivedStateOf {
			listState.firstVisibleItemIndex + 1
		}
	}

	// Replace the DatePicker Dialog block with:
	if (showDatePicker) {
		DatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			confirmButton = {
				TextButton(
					onClick = {
						val selectedDate = datePickerState.selectedDateMillis
						if (selectedDate != null) {
							val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
							val date = Instant.ofEpochMilli(selectedDate)
								.atZone(ZoneId.of("UTC"))
								.toLocalDate()
							filterDate = date.format(formatter)
						}
						showDatePicker = false
					}
				) {
					Text("OK")
				}
			},
			dismissButton = {
				TextButton(
					onClick = {
						showDatePicker = false
					}
				) {
					Text("Cancel")
				}
			}
		) {
			DatePicker(state = datePickerState)
		}
	}

	Column(
		verticalArrangement = Arrangement.Top,
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.background(backgroundColor)
			.fillMaxSize()
	) {
		// Custom header row
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			Text(
				text = "Welcome ${state.user}",
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
				if (state.tasks.isNotEmpty()) {
					Text(
						text = "$currentItem/${state.tasks.size}",
						style = TextStyle(
							fontSize = 14.sp,
							color = textColor,
							fontWeight = FontWeight.Normal
						),
						modifier = Modifier.padding(end = 8.dp)
					)
				}
				// Filter toggle icon
				IconButton(
					onClick = {
						isFilterVisible = !isFilterVisible
						if (!isFilterVisible) {
							filterDate = ""
							onAction(TaskScreenAction.OnClearFilter)
						}
					}
				) {
					Icon(
						imageVector = if (isFilterVisible) Icons.Default.FilterListOff else Icons.Default.FilterList,
						contentDescription = "Toggle Filter",
						tint = textColor
					)
				}
				// Logout icon
				IconButton(
					onClick = {
						onAction(TaskScreenAction.OnLogoutClick)
					}
				) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.ExitToApp,
						contentDescription = "Logout",
						tint = textColor
					)
				}
			}
		}

		Column(
			modifier = Modifier.padding(horizontal = 16.dp)
		) {
			// Filter TextField (hidden by default)
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
						onValueChange = { filterDate = it },
						label = { Text("Filter by date (e.g., 2025-12-25)") },
						modifier = Modifier.weight(1f),
						singleLine = true,
						trailingIcon = {
							IconButton(onClick = { showDatePicker = true }) {
								Icon(
									imageVector = Icons.Default.DateRange,
									contentDescription = "Select date"
								)
							}
						}
					)
					Spacer(modifier = Modifier.width(8.dp))
					IconButton(
						onClick = {
							if (filterDate.isNotBlank()) {
								onAction(TaskScreenAction.OnFilterByDate(filterDate))
							}
						}
					) {
						Icon(
							imageVector = Icons.Default.Search,
							contentDescription = "Search",
							tint = textColor
						)
					}
				}
			}

			if (state.loading) {
				LoadingScreen()
			} else {
				LazyColumn(
					state = listState,
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
								task = task,
								isDarkTheme = isDarkTheme
							)
						}
					}

					// Pagination: Load more when reaching the end
					if (state.canLoadMore) {
						item {
							LaunchedEffect(Unit) {
								onAction(TaskScreenAction.LoadMoreTasks)
							}
							if (state.loadingMore) {
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
	}
}

@Composable
fun TaskItem(
	modifier: Modifier = Modifier,
	task: Task,
	isDarkTheme: Boolean
) {
	val borderColor = if (isDarkTheme) Color.DarkGray else Color.White
	Column(
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.Start,
		modifier = modifier
			.background(color = borderColor)
			.shadowModifier(outsideColor = borderColor)
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