package com.cericatto.skillpulse.ui

import com.cericatto.skillpulse.R
import com.cericatto.skillpulse.domain.errors.DataError

fun DataError.asUiText(): UiText {
	return when (this) {
		// Firebase
		DataError.Firebase.LOGIN -> UiText.StringResource(
			R.string.login_failed
		)
		DataError.Firebase.SIGN_UP -> UiText.StringResource(
			R.string.signup_failed
		)
		DataError.Firebase.USER_LOGGED -> UiText.StringResource(
			R.string.user_is_logged_failed
		)

		// Local
		DataError.Local.DISK_FULL -> UiText.StringResource(
			R.string.error_disk_full
		)

		DataError.Local.USER_IS_NULL -> UiText.StringResource(
			R.string.error_user_is_null
		)

		// Network
		DataError.Network.UNAUTHORIZED -> UiText.StringResource(
			R.string.unauthorized
		)

		DataError.Network.FORBIDDEN -> UiText.StringResource(
			R.string.forbidden
		)

		DataError.Network.INTERNAL_SERVER_ERROR -> UiText.StringResource(
			R.string.try_again_later
		)

		DataError.Network.NOT_IMPLEMENTED -> UiText.StringResource(
			R.string.try_again_later
		)

		DataError.Network.BAD_GATEWAY -> UiText.StringResource(
			R.string.try_again_later
		)

		DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(
			R.string.try_again_later
		)

		DataError.Network.GATEWAY_TIMEOUT -> UiText.StringResource(
			R.string.try_again_later
		)

		DataError.Network.NO_INTERNET -> UiText.StringResource(
			R.string.no_internet
		)

		DataError.Network.SERVER_ERROR -> UiText.StringResource(
			R.string.try_again_later
		)

		DataError.Network.SERIALIZATION -> UiText.StringResource(
			R.string.error_serialization
		)

		DataError.Network.UNKNOWN -> UiText.StringResource(
			R.string.unknown_error
		)
	}
}