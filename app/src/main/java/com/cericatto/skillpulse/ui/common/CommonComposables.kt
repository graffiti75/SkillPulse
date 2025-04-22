package com.cericatto.skillpulse.ui.common

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.cericatto.skillpulse.ui.MessageAlert

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

@Composable
fun BottomAlert(
	onDismiss: () -> Unit,
	alert: MessageAlert
) {
	val messageHeight by animateDpAsState(
		// The target value is determined by the performAnimation state.
		targetValue = 100.dp,
		animationSpec = tween(
			durationMillis = 600,
			easing = FastOutSlowInEasing
		),
		label = "Error Message Height Animation"
	)
	val backgroundColor = if (alert.errorMessage != null) {
		Color(0xFFA40019)
	} else {
		Color(0xFF0F930F)
	}
	val message = if (alert.successMessage != null) {
		alert.successMessage.asString()
	} else {
		if (alert.errorMessage != null) {
			val (errorMessage, errorCause) = alert.errorMessage
			"Error: ${errorMessage.asString()}, Cause: $errorCause"
		} else ""
	}
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.clickable {
				onDismiss()
			}
			.background(backgroundColor)
			.height(messageHeight)
			// Add clip modifier to ensure content is clipped during animation.
			.clip(RectangleShape)
	) {
		Spacer(modifier = Modifier.padding(top = 10.dp))
		Text(
			text = message,
			style = TextStyle(
				color = Color.White,
				textAlign = TextAlign.Center,
				fontSize = 16.sp
			),
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.padding(bottom = 20.dp))
		HorizontalDivider(
			modifier = Modifier
				.padding(horizontal = 140.dp)
				.background(
					color = Color.White,
					shape = RoundedCornerShape(30.dp)
				)
				.height(5.dp)
		)
		Spacer(modifier = Modifier.padding(bottom = 10.dp))
	}
}

fun Modifier.shadowModifier(
	cornerShapePadding: Dp = 20.dp,
	elevation: Dp = 5.dp,
	outsideColor: Color,
	borderColor: Color = Color.Gray,
	contentColor: Color = Color.LightGray.copy(alpha = 0.5f)
) = this
	.shadow(
		elevation = elevation,
		ambientColor = outsideColor,
		spotColor = outsideColor,
		shape = RoundedCornerShape(cornerShapePadding),
	)
	.padding(2.dp)
	.background(
		color = borderColor,
		shape = RoundedCornerShape(cornerShapePadding)
	)
	.padding(2.dp)
	.background(
		color = contentColor,
		shape = RoundedCornerShape(cornerShapePadding)
	)
	.fillMaxWidth()
	.padding(15.dp)

@Composable
fun LoadingScreen() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Transparent),
		contentAlignment = Alignment.Center
	) {
		CircularProgressIndicator(
			color = MaterialTheme.colorScheme.primary,
			strokeWidth = 4.dp,
			modifier = Modifier.size(64.dp)
		)
	}
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
	LoadingScreen()
}