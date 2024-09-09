package com.example.eventmanagement.ui.fragments.profile

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import com.example.eventmanagement.ui.fragments.events.EventsViewModel
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val instantExecutorRule =
        InstantTaskExecutorRule()


    private val loginSignUpMethods: LoginSignUpMethods=mock()
    private val preferencesUtil: PreferencesUtil=mock()
    private val userDataMethods: UserDataMethods=mock()
    private val pendingOperationDao: PendingOperationDao=mock()

    private lateinit var viewModel: ProfileViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel=ProfileViewModel(
            loginSignUpMethods,
            preferencesUtil,
            userDataMethods,
            pendingOperationDao
        )
        Dispatchers.setMain(testDispatcher)
    }
    @Test
    fun `getProfileStates should return correct initial state`() {
        val initialState = viewModel.profileStates.value
        assertTrue(initialState is Response.Loading)
    }

    @Test
    fun `signOut should set state to Success when sign out is successful`() {
        // Mock behavior
        whenever(loginSignUpMethods.signOut(any())).thenAnswer {
            val callback = it.arguments[0] as (Boolean) -> Unit
            callback(true)
        }

        viewModel.signOut()

        assertTrue(viewModel.profileStates.value is Response.Success)
        verify(preferencesUtil).deleteUser()
        verify(userDataMethods).removeCurrentUserListener()
    }


    @Test
    fun `signOut should set state to Error when sign out fails`() {
        // Mock behavior
        whenever(loginSignUpMethods.signOut(any())).thenAnswer {
            val callback = it.arguments[0] as (Boolean) -> Unit
            callback(false)
        }

        viewModel.signOut()

        assertTrue(viewModel.profileStates.value is Response.Error)
    }

    @Test
    fun `updateUserLocation should insert or update pending operation`() = runBlocking {
        val userId = "user123"
        val newLocation = "New Location"

        // Mock DAO behavior
        whenever(pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_location")).thenReturn(0)

        viewModel.updateUserLocation(userId, newLocation) {}

        verify(pendingOperationDao).insert(any())
    }


    @Test
    fun `updateUserProfileStatus should insert or update pending operation`()= runBlocking {
        val userId = "user123"
        val newSetting = true

        // Mock DAO behavior
        whenever(pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_profile_status")).thenReturn(0)

        viewModel.updateUserProfileStatus(userId, newSetting) {}

        verify(pendingOperationDao).insert(any())
    }

    @Test
    fun `updateUserNotificationStatus should insert or update pending operation`() = runBlocking {
        val userId = "user123"
        val newSetting = false

        // Mock DAO behavior
        whenever(pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_notification_status")).thenReturn(0)

        // Perform the operation
        viewModel.updateUserNotificationStatus(userId, newSetting) {}

        // Verify DAO interaction
        verify(pendingOperationDao, times(1)).insert(
            check { operation ->
                assertNotNull(operation) // Ensure it is not null
                assertEquals(OperationType.UPDATE, operation.operationType)
                assertEquals(userId, operation.documentId) // Assuming documentId should be userId
                assertEquals("user_notification_status", operation.dataType) // Assuming dataType should be "user_notification_status"
                assertEquals(newSetting.toString(), operation.data) // Assuming data should be newSetting
            }
        )
    }

}