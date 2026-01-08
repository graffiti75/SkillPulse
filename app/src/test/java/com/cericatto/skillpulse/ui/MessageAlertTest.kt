package com.cericatto.skillpulse.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MessageAlertTest {

	@Test
	fun `default MessageAlert has null values`() {
		val alert = MessageAlert()

		assertThat(alert.errorMessage).isNull()
		assertThat(alert.successMessage).isNull()
	}

	@Test
	fun `MessageAlert with error message`() {
		val errorText = UiText.DynamicString("Error occurred")
		val errorDetails = "Additional details"

		val alert = MessageAlert(
			errorMessage = Pair(errorText, errorDetails)
		)

		assertThat(alert.errorMessage).isNotNull()
		assertThat(alert.errorMessage?.first).isEqualTo(errorText)
		assertThat(alert.errorMessage?.second).isEqualTo(errorDetails)
		assertThat(alert.successMessage).isNull()
	}

	@Test
	fun `MessageAlert with success message`() {
		val successText = UiText.DynamicString("Operation successful")

		val alert = MessageAlert(
			successMessage = successText
		)

		assertThat(alert.successMessage).isNotNull()
		assertThat(alert.successMessage).isEqualTo(successText)
		assertThat(alert.errorMessage).isNull()
	}

	@Test
	fun `MessageAlert can have both error and success (though unusual)`() {
		val errorText = UiText.DynamicString("Error")
		val successText = UiText.DynamicString("Success")

		val alert = MessageAlert(
			errorMessage = Pair(errorText, "details"),
			successMessage = successText
		)

		assertThat(alert.errorMessage).isNotNull()
		assertThat(alert.successMessage).isNotNull()
	}

	@Test
	fun `MessageAlert equality works correctly`() {
		val errorText = UiText.DynamicString("Error")
		val alert1 = MessageAlert(errorMessage = Pair(errorText, "details"))
		val alert2 = MessageAlert(errorMessage = Pair(errorText, "details"))

		assertThat(alert1).isEqualTo(alert2)
	}

	@Test
	fun `MessageAlert inequality with different error messages`() {
		val alert1 = MessageAlert(
			errorMessage = Pair(UiText.DynamicString("Error 1"), "details 1")
		)
		val alert2 = MessageAlert(
			errorMessage = Pair(UiText.DynamicString("Error 2"), "details 2")
		)

		assertThat(alert1).isNotEqualTo(alert2)
	}

	@Test
	fun `MessageAlert copy creates new instance`() {
		val original = MessageAlert(
			errorMessage = Pair(UiText.DynamicString("Original"), "details")
		)

		val copied = original.copy(
			successMessage = UiText.DynamicString("Added success")
		)

		assertThat(copied.errorMessage).isEqualTo(original.errorMessage)
		assertThat(copied.successMessage).isNotNull()
		assertThat(original.successMessage).isNull()
	}

	@Test
	fun `MessageAlert with empty error details`() {
		val alert = MessageAlert(
			errorMessage = Pair(UiText.DynamicString("Error"), "")
		)

		assertThat(alert.errorMessage?.second).isEmpty()
	}

	@Test
	fun `MessageAlert error message first component is UiText`() {
		val uiText = UiText.DynamicString("Dynamic error")
		val alert = MessageAlert(
			errorMessage = Pair(uiText, "details")
		)

		assertThat(alert.errorMessage?.first).isInstanceOf(UiText.DynamicString::class.java)
	}
}