package com.cericatto.skillpulse.fakes

import com.cericatto.skillpulse.domain.auth.UserAuthentication
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result

class FakeUserAuthentication : UserAuthentication {

	var shouldReturnError = false
	var errorToReturn: DataError = DataError.Firebase.LOGIN
	var isLoggedIn = false
	var currentUserEmail = "test@example.com"

	override suspend fun login(email: String, password: String): Result<Boolean, DataError> {
		return if (shouldReturnError) {
			Result.Error(errorToReturn, "Fake login error")
		} else {
			isLoggedIn = true
			currentUserEmail = email
			Result.Success(true)
		}
	}

	override suspend fun logout(): Result<Boolean, DataError> {
		return if (shouldReturnError) {
			Result.Error(DataError.Firebase.LOGOUT, "Fake logout error")
		} else {
			isLoggedIn = false
			Result.Success(true)
		}
	}

	override suspend fun signUp(email: String, password: String): Result<Boolean, DataError> {
		return if (shouldReturnError) {
			Result.Error(DataError.Firebase.SIGN_UP, "Fake sign up error")
		} else {
			isLoggedIn = true
			currentUserEmail = email
			Result.Success(true)
		}
	}

	override suspend fun userLogged(): Result<String, DataError> {
		return if (shouldReturnError) {
			Result.Error(DataError.Firebase.USER_LOGGED, "Fake user logged error")
		} else if (isLoggedIn) {
			Result.Success(currentUserEmail)
		} else {
			Result.Error(DataError.Local.USER_IS_NULL, "User not logged in")
		}
	}
}
