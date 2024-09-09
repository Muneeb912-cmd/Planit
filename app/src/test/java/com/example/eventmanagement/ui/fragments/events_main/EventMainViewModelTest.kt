package com.example.eventmanagement.ui.fragments.events_main

import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.capture

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class EventMainViewModelTest {

    private var loginSignUpMethods: LoginSignUpMethods=mock()
    private  var preferencesUtil: PreferencesUtil=mock()
    private  var userDataMethods: UserDataMethods=mock()
    private  var eventMainViewModel: EventMainViewModel=mock()


    private var signOutCallbackCaptor: ArgumentCaptor<(Boolean) -> Unit> =
        ArgumentCaptor.forClass(Function1::class.java) as ArgumentCaptor<(Boolean) -> Unit>

    @BeforeEach
    fun setup() {
        eventMainViewModel = EventMainViewModel(loginSignUpMethods, preferencesUtil, userDataMethods)
    }

    @Test
    fun `signOut - successful sign out`() {
        // Arrange
        doNothing().`when`(loginSignUpMethods).signOut(capture(signOutCallbackCaptor))

        // Act
        var result = false
        var message = ""
        eventMainViewModel.signOut { success, msg ->
            result = success
            message = msg
        }
        signOutCallbackCaptor.value.invoke(true)

        // Assert
        verify(preferencesUtil).deleteUser()
        verify(userDataMethods).removeCurrentUserListener()
        assertTrue(result)
        assertEquals("User Signed Out", message)
    }

    @Test
    fun `signOut - unsuccessful sign out`() {
        // Arrange
        doNothing().`when`(loginSignUpMethods).signOut(capture(signOutCallbackCaptor))

        // Act
        var result = false
        var message = ""
        eventMainViewModel.signOut { success, msg ->
            result = success
            message = msg
        }
        signOutCallbackCaptor.value.invoke(false)

        // Assert
        verify(preferencesUtil, never()).deleteUser()
        verify(userDataMethods, never()).removeCurrentUserListener()
        assertFalse(result)
        assertEquals("", message)
    }

    @Test
    fun `signOut - exception during sign out`() {
        // Arrange
        doThrow(RuntimeException("Error Signing Out User")).`when`(loginSignUpMethods).signOut(any())

        // Act
        var result = false
        var message = ""
        eventMainViewModel.signOut { success, msg ->
            result = success
            message = msg
        }

        // Assert
        verify(preferencesUtil, never()).deleteUser()
        verify(userDataMethods, never()).removeCurrentUserListener()
        assertFalse(result)
        assertEquals("Error Signing Out User", message)
    }
}