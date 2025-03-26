package com.cericatto.skillpulse.data.timber

import timber.log.Timber

class CustomTree : Timber.Tree() {
	override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
		// Custom logic, e.g., send logs to a server or file
		println("[$tag] $message")
	}
}