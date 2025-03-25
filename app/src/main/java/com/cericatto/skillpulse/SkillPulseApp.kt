package com.cericatto.skillpulse

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SkillPulseApp : Application() {
	override fun onCreate() {
		super.onCreate()
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