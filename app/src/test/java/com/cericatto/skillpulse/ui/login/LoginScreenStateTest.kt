package com.cericatto.skillpulse.ui.login

import com.cericatto.skillpulse.ui.MessageAlert
import com.cericatto.skillpulse.ui.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LoginScreenStateTest {

	@Test
	fun `default state has correct initial values`() {
		val state = LoginScreenState()

		assertThat(state.loading).isTrue()
		assertThat(state.alert).isNull()
		assertThat(state.user).isEmpty()
		assertThat(state.userLogged).isFalse()
	}

	@Test
	fun `state with custom values`() {
		val alert = MessageAlert(successMessage = UiText.DynamicString("Welcome"))

		val state = LoginScreenState(
			loading = false,
			alert = alert,
			user = "test@example.com",
			userLogged = true
		)

		assertThat(state.loading).isFalse()
		assertThat(state.alert).isEqualTo(alert)
		assertThat(state.user).isEqualTo("test@example.com")
		assertThat(state.userLogged).isTrue()
	}

	@Test
	fun `state copy preserves unchanged values`() {
		val original = LoginScreenState(
			user = "original@test.com",
			userLogged = true
		)

		val copied = original.copy(loading = false)

		assertThat(copied.user).isEqualTo("original@test.com")
		assertThat(copied.userLogged).isTrue()
		assertThat(copied.loading).isFalse()
	}

	@Test
	fun `state equality works correctly`() {
		val state1 = LoginScreenState(user = "same@test.com", userLogged = true)
		val state2 = LoginScreenState(user = "same@test.com", userLogged = true)

		assertThat(state1).isEqualTo(state2)
	}

	@Test
	fun `state inequality with different user`() {
		val state1 = LoginScreenState(user = "user1@test.com")
		val state2 = LoginScreenState(user = "user2@test.com")

		assertThat(state1).isNotEqualTo(state2)
	}

	@Test
	fun `state inequality with different userLogged`() {
		val state1 = LoginScreenState(userLogged = true)
		val state2 = LoginScreenState(userLogged = false)

		assertThat(state1).isNotEqualTo(state2)
	}

	@Test
	fun `state with error alert`() {
		val errorAlert = MessageAlert(
			errorMessage = Pair(UiText.DynamicString("Login failed"), "Invalid credentials")
		)

		val state = LoginScreenState(alert = errorAlert)

		assertThat(state.alert).isNotNull()
		assertThat(state.alert?.errorMessage).isNotNull()
	}

	@Test
	fun `state with success alert`() {
		val successAlert = MessageAlert(
			successMessage = UiText.DynamicString("Account created")
		)

		val state = LoginScreenState(alert = successAlert)

		assertThat(state.alert).isNotNull()
		assertThat(state.alert?.successMessage).isNotNull()
	}

	@Test
	fun `state represents loading during initial check`() {
		val state = LoginScreenState() // Default state

		assertThat(state.loading).isTrue()
		assertThat(state.userLogged).isFalse()
	}

	@Test
	fun `state represents completed check with logged user`() {
		val state = LoginScreenState(
			loading = false,
			user = "logged@test.com",
			userLogged = true
		)

		assertThat(state.loading).isFalse()
		assertThat(state.userLogged).isTrue()
		assertThat(state.user).isNotEmpty()
	}

	@Test
	fun `state represents completed check with no logged user`() {
		val state = LoginScreenState(
			loading = false,
			userLogged = false
		)

		assertThat(state.loading).isFalse()
		assertThat(state.userLogged).isFalse()
	}
}
