package com.cericatto.skillpulse.data.auth

import com.cericatto.skillpulse.domain.auth.UserAuthentication
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseUserAuthentication(
	private val auth: FirebaseAuth
): UserAuthentication {
	override suspend fun login(email: String, password: String): Result<Boolean, DataError> {
		return try {
			auth.signInWithEmailAndPassword(email, password).await()
			Result.Success(data = true)
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.LOGIN,
				message = e.message
			)
		}
	}

	override suspend fun signUp(email: String, password: String): Result<Boolean, DataError> {
		return try {
			auth.createUserWithEmailAndPassword(email, password).await()
			Result.Success(data = true)
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.SIGN_UP,
				message = e.message
			)
		}
	}

	override suspend fun isUserLogged(): Result<String, DataError> {
		return try {
			val user = auth.currentUser
			if (user != null) {
				val email = user.email ?: ""
				Result.Success(data = email)
			} else {
				Result.Success(data = "")
			}
		} catch (e: Exception) {
			Result.Error(
				error = DataError.Firebase.USER_LOGGED,
				message = e.message
			)
		}
	}
}