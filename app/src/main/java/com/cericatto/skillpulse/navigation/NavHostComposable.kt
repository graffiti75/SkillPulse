package com.cericatto.skillpulse.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cericatto.skillpulse.ui.login.LoginScreenRoot

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
			LoginScreenRoot(modifier)
		}
	}
}