package com.example.eventmanagement.ui.fragments.signup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import com.example.eventmanagement.utils.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @get:Rule
    val instantExecutorRule =
        InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    @Mock
    lateinit var validators: Validators

    @Mock
    lateinit var loginSignUpMethods: LoginSignUpMethods

    @Mock
    lateinit var userDataMethods: UserDataMethods

    @Mock
    lateinit var preferencesUtil: PreferencesUtil

    @InjectMocks
    lateinit var viewModel: SignUpViewModel


    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel =
            SignUpViewModel(validators, loginSignUpMethods, userDataMethods, preferencesUtil)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher to its original state
        testDispatcher.cleanupTestCoroutines() // Clean up coroutines in the test dispatcher
    }

    @Test
    fun getUser() = runTest {
        val testUser = User.UserData(
            userName = "Test User",
            userEmail = "test@example.com",
            userPhone = "+123456789"
        )
        `when`(userDataMethods.getCurrentUser()).thenReturn(testUser)
        val currentUser = viewModel.getCurrentUser()
        assertEquals(testUser, currentUser)
        verify(userDataMethods).getCurrentUser()
    }

    @Test
    fun isDataComplete() {
        viewModel.updateUserInfo("fullName", "John Doe")
        viewModel.updateUserInfo("email", "johndoe@example.com")
        viewModel.updateUserInfo("phone", "+923456789021")
        viewModel.updateUserInfo("dob", "01-01-2000")
        viewModel.updateUserInfo("password", "Password123!")
        viewModel.updateUserInfo("img", "path/to/profile.jpg")
        viewModel.updateUserInfo("loginType", "email_password")
        assertTrue(viewModel.isDataComplete)
    }

    @Test
    fun setDataComplete() {
        viewModel.updateUserInfo("fullName", "John Doe")
        viewModel.updateUserInfo("email", "")
        viewModel.updateUserInfo("phone", "+1234567890")
        viewModel.updateUserInfo("loginType", "email_password")

        assertFalse(viewModel.isDataComplete)

        viewModel.updateUserInfo("email", "johndoe@example.com")
        viewModel.updateUserInfo("dob", "01-01-2000")
        viewModel.updateUserInfo("password", "Password123!")
        viewModel.updateUserInfo("img", "path/to/profile.jpg")
        viewModel.updateUserInfo("loginType", "email_password")

        assertTrue(viewModel.isDataComplete)
    }

    @Test
    fun `test email verification success`() = runTest {
        val callbackCaptor = argumentCaptor<(Boolean) -> Unit>()
        whenever(loginSignUpMethods.checkEmailVerification(callbackCaptor.capture())).doAnswer {
            callbackCaptor.firstValue(true)
        }

        viewModel.checkVerificationEmail()

        val successState = viewModel.emailVerificationStatus.first()
        assertEquals(Response.Success(Unit), successState)
        assertEquals(true, viewModel.isEmailVerified)
    }

    @Test
    fun `test email verification failure`() = runTest {
        val callbackCaptor = argumentCaptor<(Boolean) -> Unit>()
        whenever(loginSignUpMethods.checkEmailVerification(callbackCaptor.capture())).doAnswer {
            callbackCaptor.firstValue(false) // Simulate failed email verification
        }

        viewModel.checkVerificationEmail()

        val errorState = viewModel.emailVerificationStatus.first()
        val actualMessage = (errorState as? Response.Error)?.exception?.message
        assertEquals("Email not verified", actualMessage)
        assertEquals(false, viewModel.isEmailVerified)
    }

    @Test
    fun getErrors_initialValue() = runTest {
        val errors = viewModel.errors.first()
        assertTrue(errors.isEmpty())
    }

    @Test
    fun getSignUpResult_initialValue() = runTest {
        val signUpResult = viewModel.signUpResult.first()
        assertEquals(Response.Loading, signUpResult)
    }

    @Test
    fun getEmailVerificationStatus_initialValue() = runTest {
        val emailVerificationStatus = viewModel.emailVerificationStatus.first()
        assertEquals(Response.Loading, emailVerificationStatus)
    }

    @Test
    fun updateUserInfo_updatesCorrectly() = runTest {
        viewModel.updateUserInfo("fullName", "John Doe")
        val user = viewModel.user.first()
        assertEquals("John Doe", user.userName)
    }

    @Test
    fun createUserAccount_signUpWithEmailPassword_success() = runTest {
        // Mock successful signup
        doAnswer { invocation ->
            // Capture the callback argument (3rd argument)
            val callback = invocation.getArgument<(Result<String>) -> Unit>(2)
            // Trigger the callback with a success result (mock userId)
            callback(Result.success("testUserId"))
            null // Return null as the method itself returns Unit
        }.`when`(loginSignUpMethods).signUpWithEmailPassword(anyString(), anyString(), any())

        // Call the function to test
        viewModel.createUserAccount()

        // Wait for the result from the signUpResult state flow
        val signUpResult = viewModel.signUpResult.first()

        // Assert that the result is a success
        assertTrue(signUpResult is Response.Success)
    }


    @Test
    fun createUserAccount_signUpWithEmailPassword_failure() = runTest {
        // Mock failed signup
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Result<String?>) -> Unit>(1)
            callback(Result.failure(Exception("Signup failed")))
        }.`when`(loginSignUpMethods).signUpWithEmailPassword(anyString(), anyString(), any())

        viewModel.createUserAccount()
        val signUpResult = viewModel.signUpResult.first()
        assertTrue(signUpResult is Response.Error)
    }

    @Test
    fun checkVerificationEmail_success() = runTest {
        // Mock email verification success
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(0)
            callback(true)
        }.`when`(loginSignUpMethods).checkEmailVerification(any())

        viewModel.checkVerificationEmail()
        val emailVerificationStatus = viewModel.emailVerificationStatus.first()
        assertTrue(emailVerificationStatus is Response.Success)
    }

    @Test
    fun checkVerificationEmail_failure() = runTest {
        // Mock email verification failure
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean) -> Unit>(0)
            callback(false)
        }.`when`(loginSignUpMethods).checkEmailVerification(any())

        viewModel.checkVerificationEmail()
        val emailVerificationStatus = viewModel.emailVerificationStatus.first()
        assertTrue(emailVerificationStatus is Response.Error)
    }

    @Test
    fun sendVerificationEmail_success() = runTest {
        // Mock successful verification email
        doNothing().`when`(loginSignUpMethods).sendVerificationEmail()

        viewModel.sendVerificationEmail()
        val signUpResult = viewModel.signUpResult.first()
        assertTrue(signUpResult is Response.Success)
    }

    @Test
    fun getCurrentUser_returnsUser() {
        val userData = User.UserData(userName = "John Doe")
        `when`(userDataMethods.getCurrentUser()).thenReturn(userData)

        val currentUser = viewModel.getCurrentUser()
        assertEquals("John Doe", currentUser?.userName)
    }

}