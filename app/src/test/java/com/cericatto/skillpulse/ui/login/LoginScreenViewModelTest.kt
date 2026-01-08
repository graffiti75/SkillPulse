package com.cericatto.skillpulse.ui.login

import app.cash.turbine.test
import com.cericatto.skillpulse.domain.errors.DataError
import com.cericatto.skillpulse.fakes.FakeUserAuthentication
import com.cericatto.skillpulse.fakes.MainDispatcherRule
import com.cericatto.skillpulse.ui.UiEvent
import com.cericatto.skillpulse.ui.navigation.Route
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenViewModelTest {

	@get:Rule
	val mainDispatcherRule = MainDispatcherRule()

	private lateinit var viewModel: LoginScreenViewModel
	private lateinit var fakeUserAuthentication: FakeUserAuthentication

	@Before
	fun setup() {
		fakeUserAuthentication = FakeUserAuthentication()
	}

	private fun createViewModel(): LoginScreenViewModel {
		return LoginScreenViewModel(fakeUserAuthentication)
	}

	// ==================== Initialization Tests ====================

	@Test
	fun `initial state checks if user is logged`() = runTest {
		fakeUserAuthentication.isLoggedIn = true
		fakeUserAuthentication.currentUserEmail = "test@example.com"

		viewModel = createViewModel()

		viewModel.events.test {
			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
			assertThat((event as UiEvent.Navigate).route).isEqualTo(Route.TaskScreen)
		}
	}

	@Test
	fun `initial state with no logged user shows login screen`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL

		viewModel = createViewModel()

		assertThat(viewModel.state.value.loading).isFalse()
	}

	// ==================== Login Tests ====================

	@Test
	fun `OnLoginClick with valid credentials navigates to TaskScreen`() = runTest {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		viewModel = createViewModel()

		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "password123"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
			assertThat((event as UiEvent.Navigate).route).isEqualTo(Route.TaskScreen)
		}
	}

	@Test
	fun `OnLoginClick with empty email still attempts login`() = runTest {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		viewModel = createViewModel()

		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("", "password123"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
		}
	}

	@Test
	fun `OnLoginClick with error shows alert`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		viewModel = createViewModel()

		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN

		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "wrong_password"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	// ==================== Create User Tests ====================

	@Test
	fun `OnCreateUser with valid data shows success alert`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		viewModel = createViewModel()

		fakeUserAuthentication.shouldReturnError = false

		viewModel.onAction(LoginScreenAction.OnCreateUser("newuser@test.com", "password123"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.successMessage).isNotNull()
	}

	@Test
	fun `OnCreateUser with error shows error alert`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		viewModel = createViewModel()

		fakeUserAuthentication.errorToReturn = DataError.Firebase.SIGN_UP

		viewModel.onAction(LoginScreenAction.OnCreateUser("existing@test.com", "password123"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	// ==================== Alert Tests ====================

	@Test
	fun `OnDismissAlert clears alert`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		viewModel = createViewModel()

		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN
		viewModel.onAction(LoginScreenAction.OnLoginClick("test@test.com", "wrong"))

		assertThat(viewModel.state.value.alert).isNotNull()

		viewModel.onAction(LoginScreenAction.OnDismissAlert)

		assertThat(viewModel.state.value.alert).isNull()
	}

	// ==================== State Tests ====================

	@Test
	fun `state updates user email when logged in`() {
		fakeUserAuthentication.isLoggedIn = true
		fakeUserAuthentication.currentUserEmail = "logged@test.com"

		viewModel = createViewModel()

		assertThat(viewModel.state.value.user).isEqualTo("logged@test.com")
	}

	@Test
	fun `state sets loading to false after check`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL

		viewModel = createViewModel()

		assertThat(viewModel.state.value.loading).isFalse()
	}

	// ==================== Multiple Login Attempts Tests ====================

	@Test
	fun `multiple failed login attempts show latest error`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		viewModel = createViewModel()

		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN

		viewModel.onAction(LoginScreenAction.OnLoginClick("user1@test.com", "wrong1"))
		viewModel.onAction(LoginScreenAction.OnDismissAlert)
		viewModel.onAction(LoginScreenAction.OnLoginClick("user2@test.com", "wrong2"))

		assertThat(viewModel.state.value.alert).isNotNull()
	}

	@Test
	fun `successful login after failed attempt navigates`() = runTest {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		viewModel = createViewModel()

		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "wrong"))
		assertThat(viewModel.state.value.alert).isNotNull()

		viewModel.onAction(LoginScreenAction.OnDismissAlert)

		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "correct"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
		}
	}
}