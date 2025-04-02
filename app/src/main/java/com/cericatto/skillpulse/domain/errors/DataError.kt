package com.cericatto.skillpulse.domain.errors

sealed interface DataError: Error {
	enum class Network: DataError {
		UNAUTHORIZED,
		FORBIDDEN,
		INTERNAL_SERVER_ERROR,
		NOT_IMPLEMENTED,
		BAD_GATEWAY,
		SERVICE_UNAVAILABLE,
		GATEWAY_TIMEOUT,
		NO_INTERNET,
		SERVER_ERROR,
		SERIALIZATION,
		UNKNOWN,
	}
	enum class Local: DataError {
		DISK_FULL,
		USER_IS_NULL
	}
	enum class Firebase: DataError {
		LOGIN,
		SIGN_UP,
		USER_LOGGED,
		FIRESTORE_ERROR
	}
}

fun <T> checkHttpException(errorCode: Int): Result<T, DataError.Network> {
	return when (errorCode) {
		401 -> Result.Error(DataError.Network.UNAUTHORIZED)
		403 -> Result.Error(DataError.Network.FORBIDDEN)
		500 -> Result.Error(DataError.Network.INTERNAL_SERVER_ERROR)
		502 -> Result.Error(DataError.Network.BAD_GATEWAY)
		503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
		504 -> Result.Error(DataError.Network.GATEWAY_TIMEOUT)
		else -> Result.Error(DataError.Network.UNKNOWN)
	}
}