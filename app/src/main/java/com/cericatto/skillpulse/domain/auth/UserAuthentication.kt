package com.cericatto.skillpulse.domain.auth

import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.domain.errors.Result

interface UserAuthentication {
	suspend fun login(email: String, password: String): Result<Boolean, DataError>
	suspend fun logout(): Result<Boolean, DataError>
	suspend fun signUp(email: String, password: String): Result<Boolean, DataError>
	suspend fun userLogged(): Result<String, DataError>
}