package com.cericatto.skillpulse.data.auth

import com.cericatto.skillpulse.domain.auth.UserAuthentication
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseUserAuthentication(
	private val auth: FirebaseAuth
): UserAuthentication {
	override suspend fun login(email: String, password: String): Result<Boolean, DataError> {
		return try {
			auth.signInWithEmailAndPassword(email, password).await()
			Result.Success(data = true)
		} catch (e: SecurityException) {
			Timber.e(e, "Security exception during login - likely Google Play " +
				"Services issue")
			Result.Error(
				error = DataError.Firebase.GOOGLE_PLAY_SERVICES,
				message = "Google Play Services error: ${e.message}"
			)
		} catch (e: Exception) {
			Timber.e(e, "Unexpected error during login")
			Result.Error(
				error = DataError.Firebase.LOGIN,
				message = e.message
			)
		}
	}

	override suspend fun logout(): Result<Boolean, DataError> {
		return try {
			auth.signOut()
			Result.Success(data = true)
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.LOGOUT,
				message = e.message
			)
		}
	}

	override suspend fun signUp(email: String, password: String): Result<Boolean, DataError> {
		return try {
			auth.createUserWithEmailAndPassword(email, password).await()
			Result.Success(data = true)
		} catch (e: SecurityException) {
			Timber.e(e, "Security exception during sign up - likely Google Play " +
				"Services issue")
			Result.Error(
				error = DataError.Firebase.GOOGLE_PLAY_SERVICES,
				message = "Google Play Services error: ${e.message}"
			)
		} catch (e: Exception) {
			Timber.e(e, "Unexpected error during sign up")
			Result.Error(
				error = DataError.Firebase.SIGN_UP,
				message = e.message
			)
		}
	}

	override suspend fun userLogged(): Result<String, DataError> {
		return try {
			val user = auth.currentUser
			if (user != null) {
				val email = user.email ?: ""
				Result.Success(data = email)
			} else {
				Result.Error(
					error = DataError.Firebase.USER_LOGGED,
					message = "No user logged in"
				)
			}
		} catch (e: SecurityException) {
			Timber.e(e, "Security exception checking user - likely Google Play " +
				"Services issue")
			Result.Error(
				error = DataError.Firebase.GOOGLE_PLAY_SERVICES,
				message = "Google Play Services error: ${e.message}"
			)
		} catch (e: Exception) {
			Timber.e(e, "Error checking logged user")
			Result.Error(
				error = DataError.Firebase.USER_LOGGED,
				message = e.message
			)
		}
	}
}