package com.cericatto.skillpulse.ui.common

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Suppress("DEPRECATION")
@Composable
fun DynamicStatusBarColor() {
	val isDarkTheme = isSystemInDarkTheme()
	val window = (LocalView.current.context as Activity).window
	val statusBarColor = if (isDarkTheme) Color.DarkGray else Color.LightGray.copy(alpha = 0.5f)

	LaunchedEffect(isDarkTheme) {
		WindowCompat.setDecorFitsSystemWindows(window, false)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
			// Android 15+: Draw background behind status bar using WindowInsets.
			window.decorView.setBackgroundColor(statusBarColor.toArgb())
			WindowCompat.getInsetsController(window, window.decorView).apply {
				// Control icon appearance.
				isAppearanceLightStatusBars = !isDarkTheme
			}
		} else {
			// Android 14 and below: Use the legacy approach.
			window.statusBarColor = statusBarColor.toArgb()
			WindowCompat.getInsetsController(window, window.decorView).apply {
				isAppearanceLightStatusBars = !isDarkTheme
			}
		}
	}
}