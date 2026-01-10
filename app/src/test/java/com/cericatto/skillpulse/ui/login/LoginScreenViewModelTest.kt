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

	private fun createViewModelWithLoggedOutUser(): LoginScreenViewModel {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		fakeUserAuthentication.errorToReturn = DataError.Local.USER_IS_NULL
		return LoginScreenViewModel(fakeUserAuthentication)
	}

	private fun createViewModelWithLoggedInUser(email: String = "user@test.com"): LoginScreenViewModel {
		fakeUserAuthentication.isLoggedIn = true
		fakeUserAuthentication.currentUserEmail = email
		fakeUserAuthentication.shouldReturnError = false
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
	fun `OnLoginClick with empty email shows validation error`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		viewModel = createViewModel()

		viewModel.onAction(LoginScreenAction.OnLoginClick("", "password123"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("Email")
	}

	@Test
	fun `OnLoginClick with empty password shows validation error`() {
		fakeUserAuthentication.isLoggedIn = false
		fakeUserAuthentication.shouldReturnError = true
		viewModel = createViewModel()

		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", ""))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("Password")
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

	// ==================== COMPLEX TESTS: Auto-Login Flow ====================

	@Test
	fun `user already logged in triggers automatic navigation to TaskScreen`() = runTest {
		viewModel = createViewModelWithLoggedInUser("existing@user.com")

		viewModel.events.test {
			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
			assertThat((event as UiEvent.Navigate).route).isEqualTo(Route.TaskScreen)
		}
	}

	@Test
	fun `user email is stored in state when already logged in`() {
		fakeUserAuthentication.isLoggedIn = true
		fakeUserAuthentication.currentUserEmail = "stored@user.com"

		viewModel = LoginScreenViewModel(fakeUserAuthentication)

		assertThat(viewModel.state.value.user).isEqualTo("stored@user.com")
	}

	@Test
	fun `loading becomes false after checking logged user status`() {
		viewModel = createViewModelWithLoggedOutUser()

		assertThat(viewModel.state.value.loading).isFalse()
	}

	// ==================== COMPLEX TESTS: Login Flow ====================

	@Test
	fun `successful login navigates to TaskScreen`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()

		// Enable successful login
		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "password123"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
			assertThat((event as UiEvent.Navigate).route).isEqualTo(Route.TaskScreen)
		}
	}

	@Test
	fun `failed login shows error alert without navigation`() {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN

		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "wrong_password"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
	}

	// ???
	@Test
	fun `login updates isLoggedIn state in authentication`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		assertThat(fakeUserAuthentication.isLoggedIn).isFalse()

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "password"))
			awaitItem() // Navigation event
		}

		assertThat(fakeUserAuthentication.isLoggedIn).isTrue()
	}

	// ???
	@Test
	fun `login stores user email in authentication`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("newuser@test.com", "password"))
			awaitItem()
		}

		assertThat(fakeUserAuthentication.currentUserEmail).isEqualTo("newuser@test.com")
	}

	// ==================== COMPLEX TESTS: Sign Up Flow ====================

	@Test
	fun `successful signup shows success alert`() {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		viewModel.onAction(LoginScreenAction.OnCreateUser("newuser@test.com", "password123"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.successMessage).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNull()
	}

	@Test
	fun `failed signup shows error alert`() {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.errorToReturn = DataError.Firebase.SIGN_UP

		viewModel.onAction(LoginScreenAction.OnCreateUser("existing@test.com", "password"))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()
		assertThat(viewModel.state.value.alert?.successMessage).isNull()
	}

	@Test
	fun `signup updates authentication state`() {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		viewModel.onAction(LoginScreenAction.OnCreateUser("newuser@test.com", "password"))

		assertThat(fakeUserAuthentication.isLoggedIn).isTrue()
		assertThat(fakeUserAuthentication.currentUserEmail).isEqualTo("newuser@test.com")
	}

	// ==================== COMPLEX TESTS: Error Recovery ====================

	@Test
	fun `dismissing alert clears error state`() {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN

		// Trigger error
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "wrong"))
		assertThat(viewModel.state.value.alert).isNotNull()

		// Dismiss
		viewModel.onAction(LoginScreenAction.OnDismissAlert)
		assertThat(viewModel.state.value.alert).isNull()
	}

	@Test
	fun `can login after dismissing previous error`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()

		// First attempt fails
		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "wrong"))
		assertThat(viewModel.state.value.alert).isNotNull()

		// Dismiss and retry with success
		viewModel.onAction(LoginScreenAction.OnDismissAlert)
		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "correct"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
		}
	}

	// ==================== COMPLEX TESTS: State Consistency ====================

	@Test
	fun `navigation clears alert before navigating`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()

		// First trigger an error
		fakeUserAuthentication.errorToReturn = DataError.Firebase.LOGIN
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "wrong"))
		assertThat(viewModel.state.value.alert).isNotNull()

		// Dismiss error and login successfully
		viewModel.onAction(LoginScreenAction.OnDismissAlert)
		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "correct"))
			awaitItem() // Navigation event

			// Alert should be null (cleared before navigation)
			assertThat(viewModel.state.value.alert).isNull()
		}
	}

	// ==================== COMPLEX TESTS: Complete User Journey ====================

	@Test
	fun `complete flow - failed signup, successful signup, then login possible`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()

		// Step 1: Failed signup (user already exists)
		fakeUserAuthentication.errorToReturn = DataError.Firebase.SIGN_UP
		viewModel.onAction(LoginScreenAction.OnCreateUser("existing@test.com", "password"))
		assertThat(viewModel.state.value.alert?.errorMessage).isNotNull()

		// Step 2: Dismiss error
		viewModel.onAction(LoginScreenAction.OnDismissAlert)

		// Step 3: Successful signup with different email
		fakeUserAuthentication.shouldReturnError = false
		viewModel.onAction(LoginScreenAction.OnCreateUser("new@test.com", "password"))
		assertThat(viewModel.state.value.alert?.successMessage).isNotNull()

		// Step 4: Dismiss success message
		viewModel.onAction(LoginScreenAction.OnDismissAlert)

		// Step 5: Login with the new account
		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("new@test.com", "password"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
		}
	}

	@Test
	fun `rapid login attempts are handled correctly`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		// Each login attempt will emit a navigation event
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "password"))
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "password"))
		viewModel.onAction(LoginScreenAction.OnLoginClick("user@test.com", "password"))

		// All 3 attempts succeed and emit navigation events
		viewModel.events.test {
			val event1 = awaitItem()
			assertThat(event1).isInstanceOf(UiEvent.Navigate::class.java)

			val event2 = awaitItem()
			assertThat(event2).isInstanceOf(UiEvent.Navigate::class.java)

			val event3 = awaitItem()
			assertThat(event3).isInstanceOf(UiEvent.Navigate::class.java)
		}
	}

	// ==================== COMPLEX TESTS: Edge Cases ====================

	/*
	@Test
	fun `empty email and password still trigger login attempt`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("", ""))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
		}
	}
	*/

	@Test
	fun `empty email and password shows validation error`() {
		viewModel = createViewModelWithLoggedOutUser()

		viewModel.onAction(LoginScreenAction.OnLoginClick("", ""))

		assertThat(viewModel.state.value.alert).isNotNull()
		assertThat(viewModel.state.value.alert?.errorMessage?.second).contains("Email")
	}

	@Test
	fun `special characters in email are handled`() = runTest {
		viewModel = createViewModelWithLoggedOutUser()
		fakeUserAuthentication.shouldReturnError = false

		viewModel.events.test {
			viewModel.onAction(LoginScreenAction.OnLoginClick("user+tag@test.com", "password"))

			val event = awaitItem()
			assertThat(event).isInstanceOf(UiEvent.Navigate::class.java)
		}

		assertThat(fakeUserAuthentication.currentUserEmail).isEqualTo("user+tag@test.com")
	}
}