package com.cericatto.skillpulse.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cericatto.skillpulse.ui.common.BottomAlert
import com.cericatto.skillpulse.ui.common.DynamicStatusBarColor

@Composable
fun LoginScreenRoot(
	modifier: Modifier = Modifier,
	viewModel: LoginScreenViewModel = hiltViewModel()
) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val onAction = viewModel::onAction

	DynamicStatusBarColor()
	Box(
		contentAlignment = Alignment.BottomCenter,
		modifier = Modifier.fillMaxSize()
	) {
		LoginScreen(
			modifier = modifier,
			onAction = onAction,
			state = state
		)
		if (state.alert != null) {
			BottomAlert(
				alert = state.alert
			)
		}
	}
}

@Composable
private fun LoginScreen(
	modifier: Modifier = Modifier,
	onAction: (LoginScreenAction) -> Unit,
	state: LoginScreenState
) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center
	) {
		TextField(
			value = email,
			onValueChange = {
				email = it
			},
			label = { Text("Email") },
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(8.dp))
		TextField(
			value = password,
			onValueChange = {
				password = it
			},
			label = {
				Text("Password")
			},
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(16.dp))
		Button(
			onClick = {
				onAction(LoginScreenAction.OnCreateUser(email, password))
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Sign Up")
		}
		Spacer(modifier = Modifier.height(8.dp))
		Button(
			onClick = {
				onAction(LoginScreenAction.OnLoginClick(email, password))
			}, // Login
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Log In")
		}
	}
}

@Preview
@Composable
fun LoginScreenPreview() {
	LoginScreen(
		modifier = Modifier,
		onAction = {},
		state = LoginScreenState()
	)
}