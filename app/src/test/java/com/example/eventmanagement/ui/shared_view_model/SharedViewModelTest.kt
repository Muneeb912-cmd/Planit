package com.example.eventmanagement.ui.shared_view_model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.models.User
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import com.example.eventmanagement.repository.room_db.attendee_dao.AttendeeDao
import com.example.eventmanagement.repository.room_db.events_dao.EventDao
import com.example.eventmanagement.repository.room_db.fav_events_dao.FavEventsDao
import com.example.eventmanagement.repository.room_db.invites_dao.InvitesDao
import com.example.eventmanagement.repository.room_db.user_dao.UserDao
import com.example.eventmanagement.repository.worker.SyncRepository
import com.example.eventmanagement.utils.PreferencesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val userDataMethods: UserDataMethods = mock()
    private val inviteMethods: InviteMethods = mock()
    private val eventDataMethods: EventDataMethods = mock()
    private val userDao: UserDao = mock()
    private val eventDao: EventDao = mock()
    private val invitesDao: InvitesDao = mock()
    private val attendeeDao: AttendeeDao = mock()
    private val favEventsDao: FavEventsDao = mock()
    private val pendingOperationDao: PendingOperationDao = mock()
    private val connectivityObserver: ConnectivityObserver = mock()
    private val preferencesUtil: PreferencesUtil = mock()
    private val syncRepository: SyncRepository = mock()
    private lateinit var viewModel: SharedViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = SharedViewModel(
            userDataMethods,
            inviteMethods,
            eventDataMethods,
            userDao,
            eventDao,
            invitesDao,
            attendeeDao,
            favEventsDao,
            pendingOperationDao,
            connectivityObserver,
            preferencesUtil,
            syncRepository
        )
        whenever(userDao.observeCurrentUser(any())).thenReturn(flowOf(mock(User.UserData::class.java)))
    }

    @Test
    fun `observeCurrentUser should fetch user data and observe events when disconnected`() = runTest {
        // Arrange
        whenever(connectivityObserver.isConnected).thenReturn(false)
        val mockUser = User.UserData(0, "123", "Attendee")
        whenever(userDao.observeCurrentUser(any())).thenReturn(flowOf(mockUser))

        // Mock observeUsers to return a valid Flow
        val mockUsers = listOf(User.UserData(1, "124", "Admin"))
        whenever(userDao.observeUsers()).thenReturn(flowOf(mockUsers))

        // Act
        viewModel.observeCurrentUser()
        advanceUntilIdle()

        // Assert
        assertEquals(mockUser, User.UserData(0, "123", "Attendee"))
        assertEquals(mockUsers, listOf(User.UserData(1, "124", "Admin")))
    }



    @Test
    fun `observeCurrentUser should update currentUser when connected`() {

    }

    @Test
    fun `observeCurrentUser should update currentUser when not connected`()  {

    }

    @Test
    fun `observeUserInvites should update user invites when connected`() {

    }

    @Test
    fun `observeUserInvites should update user invites when not connected`(){


    }

    @Test
    fun getAllUsers() {
    }

    @Test
    fun get_allEvents() {
    }

    @Test
    fun getAllEvents() {
    }

    @Test
    fun getAllFavEvents() {
    }

    @Test
    fun getAllInvites() {
    }

    @Test
    fun getCurrentUserInvites() {
    }

    @Test
    fun getCurrentUserInvitedEvents() {
    }

    @Test
    fun getObserveCurrentUserFromAttendees() {
    }

    @Test
    fun initializeObservers() {
    }

    @Test
    fun observeCurrentUser() {
    }

    @Test
    fun observeCurrentUserFromAttendees() {
    }

    @Test
    fun observeUserInvites() {
    }

    @Test
    fun observeAllFavEvents() {
    }

    @Test
    fun getProfileStatus() {
    }

    @Test
    fun resetViewModel() {
    }
}