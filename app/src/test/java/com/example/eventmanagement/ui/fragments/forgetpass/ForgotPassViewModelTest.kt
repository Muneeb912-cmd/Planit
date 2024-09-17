package com.example.eventmanagement.ui.fragments.forgetpass

import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.capture

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ForgotPassViewModelTest {

    private var loginSignUpMethods: LoginSignUpMethods=mock()

    private var forgotPassViewModel: ForgotPassViewModel=mock()

    private var resetPasswordCallbackCaptor: ArgumentCaptor<(Boolean) -> Unit> =
        ArgumentCaptor.forClass(Function1::class.java) as ArgumentCaptor<(Boolean) -> Unit>

    @BeforeEach
    fun setup() {
        forgotPassViewModel = ForgotPassViewModel(loginSignUpMethods)
    }

    @Test
    fun `sendPasswordRestEmail - successful email send`() {
        // Arrange
        doNothing().`when`(loginSignUpMethods).sendResetPasswordEmail(any(), capture(resetPasswordCallbackCaptor))

        // Act
        var result = false
        forgotPassViewModel.sendPasswordRestEmail("test@example.com") { success ->
            result = success
        }
        resetPasswordCallbackCaptor.value.invoke(true)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `sendPasswordRestEmail - unsuccessful email send`() {
        // Arrange
        doNothing().`when`(loginSignUpMethods).sendResetPasswordEmail(any(), capture(resetPasswordCallbackCaptor))

        // Act
        var result = false
        forgotPassViewModel.sendPasswordRestEmail("test@example.com") { success ->
            result = success
        }
        resetPasswordCallbackCaptor.value.invoke(false)

        // Assert
        assertFalse(result)
    }
}
