package com.cericatto.skillpulse.ui.common

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.ui.common.utils.ConfirmationDialog
import com.cericatto.skillpulse.ui.task.TaskItem
import com.cericatto.skillpulse.ui.task.TaskScreenAction
import com.cericatto.skillpulse.ui.theme.orange
import kotlinx.coroutines.launch

@Composable
fun SwipeableTaskItem(
	modifier: Modifier = Modifier,
	item: Task,
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	onAction: (TaskScreenAction) -> Unit,
	deleteThreshold: Dp = (-150).dp,
	content: @Composable () -> Unit
) {
	val outsideColor = if (isDarkTheme) Color.DarkGray else Color.White
	var isDragging by remember { mutableStateOf(false) }
	var dragOffset by remember { mutableFloatStateOf(0f) }
	val coroutineScope = rememberCoroutineScope()
	val animatedOffset by animateDpAsState(
		targetValue = if (isDragging) dragOffset.dp else 0.dp,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		)
	)
	val backgroundColor by animateColorAsState(
		targetValue = when {
			animatedOffset < 0.dp -> {
				Color.Red.copy(
					alpha = (-animatedOffset.value / deleteThreshold.value)
						.coerceIn(0f, 1f)
				)
			}
			else -> outsideColor
		},
		animationSpec = spring(stiffness = Spring.StiffnessMedium)
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.fillMaxWidth()
	) {
		// Background layer with delete icon.
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.End,
			modifier = Modifier
				.fillMaxSize()
		) {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = "Delete",
				tint = orange,
				modifier = Modifier.padding(end = 16.dp)
					.fillMaxHeight()
			)
		}

		// Foreground content.
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.offset(x = animatedOffset)
				.pointerInput(Unit) {
					detectHorizontalDragGestures(
						onDragStart = { isDragging = true },
						onDragEnd = {
							isDragging = false
							if (animatedOffset < deleteThreshold) {
								onAction(TaskScreenAction.OnShowDeleteDialog(item))
							}
							coroutineScope.launch {
								dragOffset = 0f
							}
						},
						onDragCancel = {
							isDragging = false
							dragOffset = 0f
						}
					) { change, dragAmount ->
						change.consume()
						val newOffset =
							(dragOffset + dragAmount).coerceAtLeast(deleteThreshold.value * 2)
						if (newOffset <= 0f) { // Only allow swipe left
							dragOffset = newOffset
						}
					}
				}
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxSize()
			) {
				content()
			}
		}
	}
}

@Preview(
	name = "Light Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_NO,
	showBackground = true
)
@Composable
fun SwipeableTaskItemPreviewLight() {
	SwipeableTaskItem(
		item = Task(),
		onAction = {},
		content = {
			TaskItem(
				modifier = Modifier
					.padding(top = 5.dp),
				task = Task(),
				isDarkTheme = false
			)
		}
	)
}

@Preview(
	name = "Dark Theme Preview",
	uiMode = Configuration.UI_MODE_NIGHT_YES,
	showBackground = true
)
@Composable
fun SwipeableTaskItemPreviewDark() {
	SwipeableTaskItem(
		item = Task(),
		onAction = {},
		content = {
			TaskItem(
				modifier = Modifier
					.padding(top = 5.dp),
				task = Task(),
				isDarkTheme = true
			)
		}
	)
}