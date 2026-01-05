package com.cericatto.skillpulse.ui.edit

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cericatto.skillpulse.ui.ObserveAsEvents
import com.cericatto.skillpulse.ui.UiEvent
import com.cericatto.skillpulse.ui.common.BottomAlert
import com.cericatto.skillpulse.ui.common.DynamicStatusBarColor
import com.cericatto.skillpulse.ui.common.LoadingScreen
import com.cericatto.skillpulse.ui.navigation.Route
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun EditScreenRoot(
	onNavigate: (Route) -> Unit,
	onNavigateUp: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: EditScreenViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val onAction = viewModel::onAction

	ObserveAsEvents(viewModel.events) { event ->
		when (event) {
			is UiEvent.Navigate -> onNavigate(event.route)
			is UiEvent.NavigateUp -> onNavigateUp()
		}
	}

	DynamicStatusBarColor()
	Box(
		contentAlignment = Alignment.TopStart,
		modifier = Modifier.fillMaxSize()
	) {
		EditScreen(
			modifier = modifier,
			onAction = onAction,
			state = state
		)
		state.alert?.let {
			BottomAlert(
				onDismiss = { onAction(EditScreenAction.OnDismissAlert) },
				alert = it
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
	modifier: Modifier = Modifier,
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	onAction: (EditScreenAction) -> Unit,
	state: EditScreenState
) {
	val backgroundColor = if (isDarkTheme) Color.DarkGray else Color.White

	// Focus requesters for keyboard navigation
	val focusManager = LocalFocusManager.current
	val descriptionFocusRequester = remember { FocusRequester() }
	val startTimeFocusRequester = remember { FocusRequester() }
	val endTimeFocusRequester = remember { FocusRequester() }

	// DateTimePicker states
	var showStartDateTimePicker by remember { mutableStateOf(false) }
	var showEndDateTimePicker by remember { mutableStateOf(false) }

	// DateTimePickers

	EditScreenDateTimePicker(
		showPicker = showStartDateTimePicker,
		onDismiss = { showStartDateTimePicker = false },
		onDateTimeSelected = { selectedDateTime ->
			onAction(EditScreenAction.OnStartTimeChange(selectedDateTime))
		}
	)

	EditScreenDateTimePicker(
		showPicker = showEndDateTimePicker,
		onDismiss = { showEndDateTimePicker = false },
		onDateTimeSelected = { selectedDateTime ->
			onAction(EditScreenAction.OnEndTimeChange(selectedDateTime))
		}
	)

	Column(
		verticalArrangement = Arrangement.Top,
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = modifier
			.background(backgroundColor)
			.fillMaxSize()
	) {
		// Header
		EditScreenTopHeader(
			isDarkTheme = isDarkTheme,
			onBackClick = { onAction(EditScreenAction.OnBackClick) }
		)

		if (state.loading) {
			LoadingScreen()
		} else {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Spacer(modifier = Modifier.height(8.dp))

				// Task ID (read-only)
				OutlinedTextField(
					value = state.taskId,
					onValueChange = { },
					label = { Text("Task ID") },
					modifier = Modifier.fillMaxWidth(),
					enabled = false,
					singleLine = true,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next
					),
					keyboardActions = KeyboardActions(
						onNext = { descriptionFocusRequester.requestFocus() }
					)
				)

				// Description
				OutlinedTextField(
					value = state.description,
					onValueChange = { onAction(EditScreenAction.OnDescriptionChange(it)) },
					label = { Text("Description") },
					modifier = Modifier.fillMaxWidth()
						.focusRequester(descriptionFocusRequester),
					minLines = 3,
					maxLines = 5,
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Text,
						imeAction = ImeAction.Next
					),
					keyboardActions = KeyboardActions(
						onNext = { startTimeFocusRequester.requestFocus() }
					)
				)

				// Start Time
				OutlinedTextField(
					value = state.startTime,
					onValueChange = { onAction(EditScreenAction.OnStartTimeChange(it)) },
					label = { Text("Start Time") },
					modifier = Modifier.fillMaxWidth()
						.focusRequester(startTimeFocusRequester),
					singleLine = true,
					readOnly = true,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next
					),
					keyboardActions = KeyboardActions(
						onNext = { endTimeFocusRequester.requestFocus() }
					),
					trailingIcon = {
						IconButton(onClick = { showStartDateTimePicker = true }) {
							Icon(
								imageVector = Icons.Default.DateRange,
								contentDescription = "Select start date"
							)
						}
					}
				)

				// End Time
				OutlinedTextField(
					value = state.endTime,
					onValueChange = { onAction(EditScreenAction.OnEndTimeChange(it)) },
					label = { Text("End Time") },
					modifier = Modifier.fillMaxWidth()
						.focusRequester(endTimeFocusRequester),
					singleLine = true,
					readOnly = true,
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Done
					),
					keyboardActions = KeyboardActions(
						onDone = { focusManager.clearFocus() }
					),
					trailingIcon = {
						IconButton(onClick = { showEndDateTimePicker = true }) {
							Icon(
								imageVector = Icons.Default.DateRange,
								contentDescription = "Select end date"
							)
						}
					}
				)

				Spacer(modifier = Modifier.height(16.dp))

				// Save Button
				Button(
					onClick = { onAction(EditScreenAction.OnSaveClick) },
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = "Save Changes",
						style = TextStyle(
							fontSize = 16.sp,
							fontWeight = FontWeight.Medium
						)
					)
				}
			}
		}
	}
}

@Composable
private fun EditScreenTopHeader(
	isDarkTheme: Boolean,
	onBackClick: () -> Unit
) {
	val textColor = if (isDarkTheme) Color.LightGray else Color.Black

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp, vertical = 8.dp)
	) {
		Row(
			modifier = Modifier.align(Alignment.CenterStart),
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(onClick = onBackClick) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowBack,
					contentDescription = "Back",
					tint = textColor
				)
			}
			Text(
				text = "Edit Task",
				style = TextStyle(
					fontSize = 20.sp,
					color = textColor,
					fontWeight = FontWeight.Medium
				)
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScreenDateTimePicker(
	showPicker: Boolean,
	onDismiss: () -> Unit,
	onDateTimeSelected: (String) -> Unit
) {
	var showDatePicker by remember { mutableStateOf(true) }
	var showTimePicker by remember { mutableStateOf(false) }
	var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

	val datePickerState = rememberDatePickerState()
	val timePickerState = rememberTimePickerState(
		initialHour = 12,
		initialMinute = 0,
		is24Hour = true
	)

	if (showPicker) {
		EditScreenShowDatePicker(
			showDatePicker = showDatePicker,
			datePickerState = datePickerState,
			onDateSelected = { date ->
				selectedDate = date
				showDatePicker = false
				showTimePicker = true
			},
			onCancel = {
				showDatePicker = true
				showTimePicker = false
				onDismiss()
			}
		)

		EditScreenShowTimePicker(
			showTimePicker = showTimePicker,
			selectedDate = selectedDate,
			timePickerState = timePickerState,
			onDateTimeSelected = { dateTimeString ->
				onDateTimeSelected(dateTimeString)
				showDatePicker = true
				showTimePicker = false
				selectedDate = null
				onDismiss()
			},
			onBack = {
				showDatePicker = true
				showTimePicker = false
			},
			onCancel = {
				showDatePicker = true
				showTimePicker = false
				selectedDate = null
				onDismiss()
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScreenShowDatePicker(
	showDatePicker: Boolean,
	datePickerState: androidx.compose.material3.DatePickerState,
	onDateSelected: (LocalDate) -> Unit,
	onCancel: () -> Unit
) {
	if (showDatePicker) {
		DatePickerDialog(
			onDismissRequest = onCancel,
			confirmButton = {
				TextButton(
					onClick = {
						val selectedDateMillis = datePickerState.selectedDateMillis
						if (selectedDateMillis != null) {
							val date = Instant.ofEpochMilli(selectedDateMillis)
								.atZone(ZoneId.systemDefault())
								.toLocalDate()
							onDateSelected(date)
						}
					}
				) {
					Text("Next")
				}
			},
			dismissButton = {
				TextButton(onClick = onCancel) {
					Text("Cancel")
				}
			}
		) {
			DatePicker(state = datePickerState)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScreenShowTimePicker(
	showTimePicker: Boolean,
	selectedDate: LocalDate?,
	timePickerState: androidx.compose.material3.TimePickerState,
	onDateTimeSelected: (String) -> Unit,
	onBack: () -> Unit,
	onCancel: () -> Unit
) {
	if (showTimePicker && selectedDate != null) {
		AlertDialog(
			onDismissRequest = onCancel,
			title = { Text("Select Time") },
			text = {
				Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.Center
				) {
					TimePicker(state = timePickerState)
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						// Create ZonedDateTime with system timezone for ISO 8601 format
						val zonedDateTime = selectedDate
							.atTime(timePickerState.hour, timePickerState.minute, 0)
							.atZone(ZoneId.systemDefault())

						// Format as ISO 8601 with timezone offset (e.g., 2025-12-24T22:15:00-03:00)
						val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
						onDateTimeSelected(zonedDateTime.format(formatter))
					}
				) {
					Text("OK")
				}
			},
			dismissButton = {
				Row {
					TextButton(onClick = onBack) {
						Text("Back")
					}
					TextButton(onClick = onCancel) {
						Text("Cancel")
					}
				}
			}
		)
	}
}

@Preview(
	name = "Light Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_NO,
	showBackground = true
)
@Composable
fun EditScreenPreviewLight() {
	EditScreen(
		isDarkTheme = false,
		modifier = Modifier,
		onAction = {},
		state = EditScreenState(
			taskId = "123e4567-e89b-12d3-a456-426614174000",
			description = "Sample task description",
			startTime = "2025-01-05 10:00:00",
			endTime = "2025-01-05 18:00:00"
		)
	)
}

@Preview(
	name = "Dark Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true
)
@Composable
fun EditScreenPreviewDark() {
	EditScreen(
		isDarkTheme = true,
		modifier = Modifier,
		onAction = {},
		state = EditScreenState(
			taskId = "123e4567-e89b-12d3-a456-426614174000",
			description = "Sample task description",
			startTime = "2025-01-05 10:00:00",
			endTime = "2025-01-05 18:00:00"
		)
	)
}