package com.cericatto.skillpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cericatto.skillpulse.ui.common.SwipeToDeleteScreen
import com.cericatto.skillpulse.ui.navigation.NavHostComposable
import com.cericatto.skillpulse.ui.theme.SkillPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			SkillPulseTheme {
				val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
				Surface(
					modifier = Modifier
						.fillMaxSize()
						.padding(systemBarsPadding) // Explicitly apply padding here
				) {
					NavHostComposable(
						modifier = Modifier.fillMaxSize() // Pass a clean modifier
					)
//					SwipeToDeleteScreen(
//						modifier = Modifier.fillMaxSize()
//					)
				}
			}
		}
	}
}