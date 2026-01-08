package com.cericatto.skillpulse

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val ITEMS_LIMIT = 50
const val SUGGESTIONS_LIMIT = 10

@HiltAndroidApp
class SkillPulseApp : Application() {
	override fun onCreate() {
		super.onCreate()

		// Initialize Firebase.
		FirebaseApp.initializeApp(this)

		// Plant a debug tree for logging in debug builds
		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		} else {
			// Optionally plant a tree that does nothing or logs minimally
			Timber.plant(object : Timber.Tree() {
				override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
					// Do nothing or log to a secure location
				}
			})
		}
	}
}