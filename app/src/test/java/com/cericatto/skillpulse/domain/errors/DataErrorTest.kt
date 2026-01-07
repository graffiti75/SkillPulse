package com.cericatto.skillpulse.domain.errors

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DataErrorTest {

	// ==================== checkHttpException Tests ====================

	@Test
	fun `checkHttpException with 401 returns UNAUTHORIZED`() {
		val result = checkHttpException<String>(401)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.UNAUTHORIZED)
	}

	@Test
	fun `checkHttpException with 403 returns FORBIDDEN`() {
		val result = checkHttpException<String>(403)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.FORBIDDEN)
	}

	@Test
	fun `checkHttpException with 500 returns INTERNAL_SERVER_ERROR`() {
		val result = checkHttpException<String>(500)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.INTERNAL_SERVER_ERROR)
	}

	@Test
	fun `checkHttpException with 502 returns BAD_GATEWAY`() {
		val result = checkHttpException<String>(502)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.BAD_GATEWAY)
	}

	@Test
	fun `checkHttpException with 503 returns SERVICE_UNAVAILABLE`() {
		val result = checkHttpException<String>(503)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.SERVICE_UNAVAILABLE)
	}

	@Test
	fun `checkHttpException with 504 returns GATEWAY_TIMEOUT`() {
		val result = checkHttpException<String>(504)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.GATEWAY_TIMEOUT)
	}

	@Test
	fun `checkHttpException with unknown code returns UNKNOWN`() {
		val result = checkHttpException<String>(999)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.UNKNOWN)
	}

	@Test
	fun `checkHttpException with 404 returns UNKNOWN`() {
		val result = checkHttpException<String>(404)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.UNKNOWN)
	}

	@Test
	fun `checkHttpException with 200 returns UNKNOWN`() {
		val result = checkHttpException<String>(200)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.UNKNOWN)
	}

	// ==================== DataError Enum Tests ====================

	@Test
	fun `Network enum contains all expected values`() {
		val networkErrors = DataError.Network.entries
		assertThat(networkErrors).contains(DataError.Network.UNAUTHORIZED)
		assertThat(networkErrors).contains(DataError.Network.FORBIDDEN)
		assertThat(networkErrors).contains(DataError.Network.INTERNAL_SERVER_ERROR)
		assertThat(networkErrors).contains(DataError.Network.NOT_IMPLEMENTED)
		assertThat(networkErrors).contains(DataError.Network.BAD_GATEWAY)
		assertThat(networkErrors).contains(DataError.Network.SERVICE_UNAVAILABLE)
		assertThat(networkErrors).contains(DataError.Network.GATEWAY_TIMEOUT)
		assertThat(networkErrors).contains(DataError.Network.NO_INTERNET)
		assertThat(networkErrors).contains(DataError.Network.SERVER_ERROR)
		assertThat(networkErrors).contains(DataError.Network.SERIALIZATION)
		assertThat(networkErrors).contains(DataError.Network.UNKNOWN)
	}

	@Test
	fun `Local enum contains all expected values`() {
		val localErrors = DataError.Local.entries
		assertThat(localErrors).contains(DataError.Local.DISK_FULL)
		assertThat(localErrors).contains(DataError.Local.USER_IS_NULL)
	}

	@Test
	fun `Firebase enum contains all expected values`() {
		val firebaseErrors = DataError.Firebase.entries
		assertThat(firebaseErrors).contains(DataError.Firebase.LOGIN)
		assertThat(firebaseErrors).contains(DataError.Firebase.LOGOUT)
		assertThat(firebaseErrors).contains(DataError.Firebase.SIGN_UP)
		assertThat(firebaseErrors).contains(DataError.Firebase.USER_LOGGED)
		assertThat(firebaseErrors).contains(DataError.Firebase.FIRESTORE_ERROR)
		assertThat(firebaseErrors).contains(DataError.Firebase.GOOGLE_PLAY_SERVICES)
	}

	// ==================== Result Tests ====================

	@Test
	fun `Result Success contains data`() {
		val result: Result<String, DataError> = Result.Success("test data")
		assertThat(result).isInstanceOf(Result.Success::class.java)
		assertThat((result as Result.Success).data).isEqualTo("test data")
	}

	@Test
	fun `Result Error contains error and message`() {
		val result: Result<String, DataError> = Result.Error(
			DataError.Network.NO_INTERNET,
			"No internet connection"
		)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).error).isEqualTo(DataError.Network.NO_INTERNET)
		assertThat(result.message).isEqualTo("No internet connection")
	}

	@Test
	fun `Result Error with null message`() {
		val result: Result<String, DataError> = Result.Error(DataError.Network.UNKNOWN)
		assertThat(result).isInstanceOf(Result.Error::class.java)
		assertThat((result as Result.Error).message).isNull()
	}
}