package com.cericatto.skillpulse.ui.common.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.cericatto.skillpulse.R
import com.cericatto.skillpulse.data.model.Task
import com.cericatto.skillpulse.ui.task.TaskScreenAction

@Composable
fun ConfirmationDialog(
	item: Task,
	onAction: (TaskScreenAction) -> Unit
) {
	AlertDialog(
		onDismissRequest = {
			onAction(TaskScreenAction.OnShowDeleteDialog(false))
		},
		title = {
			Text(
				text = stringResource(R.string.dialog__delete_item)
			)
		},
		text = {
			Text(
				text = stringResource(R.string.dialog__are_you_sure_delete_item)
			)
		},
		confirmButton = {
			Button(
				onClick = {
					onAction(TaskScreenAction.OnShowDeleteDialog(false))
					onAction(TaskScreenAction.OnDeleteTask(item))
				}
			) {
				Text(
					text = stringResource(R.string.dialog__ok),
					color = Color.White
				)
			}
		},
		dismissButton = {
			Button(
				onClick = {
					onAction(TaskScreenAction.OnShowDeleteDialog(false))
				}
			) {
				Text(
					text = stringResource(R.string.dialog__cancel),
					color = Color.White
				)
			}
		}
	)
}