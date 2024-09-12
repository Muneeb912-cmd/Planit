package com.example.eventmanagement.ui.fragments.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    // For LiveData and coroutines testing
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    // Mocks
    private lateinit var loginSignUpMethods: LoginSignUpMethods
    private lateinit var userDataMethods: UserDataMethods
    private lateinit var preferencesUtil: PreferencesUtil

    // ViewModel under test
    private lateinit var loginViewModel: LoginViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        loginSignUpMethods = mock(LoginSignUpMethods::class.java)
        userDataMethods = mock(UserDataMethods::class.java)
        preferencesUtil = mock(PreferencesUtil::class.java)

        // Initialize ViewModel
        loginViewModel = LoginViewModel(loginSignUpMethods, userDataMethods, preferencesUtil)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithEmailPassword success`() = runTest {
//        // Arrange
//        val email = "test@example.com"
//        val password = "password123"
//        val isVerified = true
//
//        doAnswer { invocation ->
//            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
//            callback(isVerified, "Success")
//            null
//        }.`when`(loginSignUpMethods).signInWithEmailPassword(anyString(), anyString(), any())
//
//        doAnswer { invocation ->
//            val callback = invocation.getArgument<(Boolean) -> Unit>(0)
//            callback(isVerified)
//            null
//        }.`when`(loginSignUpMethods).checkEmailVerification(any())
//
//        val usersList = listOf(User.UserData(0, "User12445",))
//        whenever(userDataMethods.getAllUserData()).thenReturn(usersList)
//
//        // Act
//        loginViewModel.signInWithEmailPassword(email, password)
//        advanceUntilIdle() // Wait for coroutines
//
//        // Assert
//        val loginResult = loginViewModel.loginResult.first()
//        assertTrue(loginResult is Response.Success)
//
//        val usersData = loginViewModel.usersData.first()
//        assertTrue(usersData is Response.Success)
//        assertEquals(usersList, (usersData as Response.Success).data)
    }

    @Test
    fun `signInWithEmailPassword failure`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrongPassword"
        val errorMsg = "Invalid credentials"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, errorMsg)
            null
        }.`when`(loginSignUpMethods).signInWithEmailPassword(anyString(), anyString(), any())

        // Act
        loginViewModel.signInWithEmailPassword(email, password)
        advanceUntilIdle() // Wait for coroutines

        // Assert
        val loginResult = loginViewModel.loginResult.first()
        assertTrue(loginResult is Response.Error)
        assertEquals(errorMsg, (loginResult as Response.Error).exception.message)
    }

    @Test
    fun `signInWithGoogle success`() = runTest {
        // Arrange
        val account = mock(GoogleSignInAccount::class.java)
        val isVerified = true

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(1)
            callback(isVerified)
            null
        }.`when`(loginSignUpMethods).signInWithGoogle(any(), any())

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(0)
            callback(isVerified)
            null
        }.`when`(loginSignUpMethods).checkEmailVerification(any())

        val usersList = listOf(User.UserData(0, "User12445",))
        whenever(userDataMethods.getAllUserData()).thenReturn(usersList)

        // Act
        loginViewModel.signInWithGoogle(account)
        advanceUntilIdle() // Wait for coroutines

        // Assert
        val loginResult = loginViewModel.loginResult.first()
        assertTrue(loginResult is Response.Success)



        val usersData = loginViewModel.usersData.first()
        assertTrue(usersData is Response.Success)
        assertEquals(usersList, (usersData as Response.Success).data)
    }

    @Test
    fun `signInWithGoogle failure`() = runTest {
        // Arrange
        val account = mock(GoogleSignInAccount::class.java)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(1)
            callback(false)
            null
        }.`when`(loginSignUpMethods).signInWithGoogle(any(), any())

        // Act
        loginViewModel.signInWithGoogle(account)
        advanceUntilIdle() // Wait for coroutines

        // Assert
        val loginResult = loginViewModel.loginResult.first()
        assertTrue(loginResult is Response.Error)
        assertEquals("User Don't Exist", (loginResult as Response.Error).exception.message)
    }

    @Test
    fun `getCurrentUser returns user data`() {
        // Arrange
        val mockUser = User.UserData(0, "User12445",)
        whenever(userDataMethods.getCurrentUser()).thenReturn(mockUser)

        // Act
        val currentUser = loginViewModel.getCurrentUser()

        // Assert
        assertNotNull(currentUser)
        assertEquals(mockUser.userId, currentUser?.userId)
        assertEquals(mockUser.userEmail, currentUser?.userEmail)
    }

    @Test
    fun `getCurrentUser returns null when user is not logged in`() {
        // Arrange
        whenever(userDataMethods.getCurrentUser()).thenReturn(null)

        // Act
        val currentUser = loginViewModel.getCurrentUser()

        // Assert
        assertNull(currentUser)
    }

    @Test
    fun `saveDataToPreferences handles exception`() {
        // Arrange
        val userData = User.UserData(0, "User12445",)
        doThrow(RuntimeException("Error")).`when`(preferencesUtil).saveUser(any())
        var result: Boolean? = null

        // Act
        loginViewModel.saveDataToPreferences(userData) { success ->
            result = success
        }

        // Assert
        assertFalse(result == true)
    }


    @Test
    fun `checkUserInPrefs returns false when user does not exist`() {
        // Arrange
        whenever(preferencesUtil.getUser()).thenReturn(null)
        var result: Boolean? = null
        var role: String? = null

        // Act
        loginViewModel.checkUserInPrefs { success, userRole ->
            result = success
            role = userRole
        }

        // Assert
        assertFalse(result == true)
        assertEquals("null", role)
    }

    @Test
    fun `getUsersData retrieves users successfully`() = runTest {
        // Arrange
        val usersList = listOf(User.UserData(userId = "User12445", userName = "Jon Doe"), User.UserData(userId = "User12446", userName = "Jane Doe"))
        whenever(userDataMethods.getAllUserData()).thenReturn(usersList)

        // Act
        loginViewModel.signInWithEmailPassword("email", "password")
        loginViewModel.getAllUsers()
        advanceUntilIdle() // Wait for coroutines

        // Assert
        val usersData = loginViewModel.usersData.first()
        assertTrue(usersData is Response.Success)
        assertEquals(usersList, (usersData as Response.Success).data)
    }

    @Test
    fun `getLoginResult reflects correct login status`() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Success")
            null
        }.`when`(loginSignUpMethods).signInWithEmailPassword(anyString(), anyString(), any())

        // Act
        loginViewModel.signInWithEmailPassword(email, password)
        advanceUntilIdle() // Wait for coroutines

        // Assert
        val loginResult = loginViewModel.loginResult.first()
        assertTrue(loginResult is Response.Success)
    }
}
