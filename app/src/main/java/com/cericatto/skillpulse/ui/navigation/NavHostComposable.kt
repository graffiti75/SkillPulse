package com.cericatto.skillpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cericatto.skillpulse.ui.add.AddScreenRoot
import com.cericatto.skillpulse.ui.edit.EditScreenRoot
import com.cericatto.skillpulse.ui.login.LoginScreenRoot
import com.cericatto.skillpulse.ui.task.TaskScreenRoot

@Composable
fun NavHostComposable(
	modifier: Modifier = Modifier
) {
	val navController = rememberNavController()
	NavHost(
		navController = navController,
		startDestination = Route.LoginScreen
	) {
		composable<Route.LoginScreen> {
			LoginScreenRoot(
				modifier = modifier,
				onNavigate = { navController.navigate(it) },
				onNavigateUp = { navController.navigateUp() }
			)
		}
		composable<Route.TaskScreen> {
			TaskScreenRoot(
				modifier = modifier,
				onNavigate = { navController.navigate(it) },
				onNavigateUp = { navController.navigateUp() }
			)
		}
		composable<Route.EditScreen> {
			EditScreenRoot(
				modifier = modifier,
				onNavigate = { navController.navigate(it) },
				onNavigateUp = { navController.navigateUp() }
			)
		}
		composable<Route.AddScreen> {
			AddScreenRoot(
				modifier = modifier,
				onNavigate = { navController.navigate(it) },
				onNavigateUp = { navController.navigateUp() }
			)
		}
	}
}