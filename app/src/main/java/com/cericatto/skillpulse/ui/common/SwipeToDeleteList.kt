package com.cericatto.skillpulse.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.cericatto.skillpulse.ui.theme.orange
import kotlinx.coroutines.launch

@Composable
fun SwipeToDeleteScreen(
	modifier: Modifier = Modifier
) {
	var items by remember { mutableStateOf(items()) }
	Column(
		modifier = modifier
	) {
		SwipeToDeleteList(
			items = items,
			onDelete = { index ->
				items = items.toMutableList().apply {
					removeAt(index)
				}
			}
		)
	}
}

@Composable
fun SwipeToDeleteList(
	items: List<String>,
	onDelete: (Int) -> Unit
) {
	LazyColumn {
		itemsIndexed(items) { index, item ->
			SwipeableListItem(
				item = item,
				onDelete = { onDelete(index) }
			)
		}
	}
}

@Composable
fun SwipeableListItem(
	item: String,
	onDelete: () -> Unit,
	deleteThreshold: Dp = (-150).dp
) {
	var isDragging by remember { mutableStateOf(false) }
	var dragOffset by remember { mutableStateOf(0f) }
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
			animatedOffset < 0.dp -> Color.Red.copy(alpha = (-animatedOffset.value / deleteThreshold.value).coerceIn(0f, 1f))
			else -> Color.Transparent
		},
		animationSpec = spring(stiffness = Spring.StiffnessMedium)
	)

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
	) {
		// Background layer with delete icon.
		Row(
			modifier = Modifier
				.fillMaxSize()
				.background(backgroundColor),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.End
		) {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = "Delete",
				tint = orange,
				modifier = Modifier.padding(end = 16.dp)
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
								onDelete()
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
						val newOffset = (dragOffset + dragAmount).coerceAtLeast(deleteThreshold.value * 2)
						if (newOffset <= 0f) { // Only allow swipe left
							dragOffset = newOffset
						}
					}
				}
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.background(Color.Gray)
					.fillMaxSize()
					.padding(10.dp)
			) {
				Text(text = item)
			}
		}
	}
}

@Preview
@Composable
fun SwipeToDeleteListPreview() {
	SwipeToDeleteList(
		items = items(),
		onDelete = {}
	)
}

@Preview
@Composable
fun SwipeableListItemPreview() {
	SwipeableListItem(
		item = "Item 1",
		onDelete = {}
	)
}

fun items() = List(20) { "Item $it" }