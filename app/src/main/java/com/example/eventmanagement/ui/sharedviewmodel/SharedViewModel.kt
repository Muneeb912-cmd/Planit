package com.example.eventmanagement.ui.sharedviewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.EventInvites
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.repository.roomDb.attendee_dao.AttendeeDao
import com.example.eventmanagement.repository.roomDb.events_dao.EventDao
import com.example.eventmanagement.repository.roomDb.fav_events_dao.FavEventsDao
import com.example.eventmanagement.repository.roomDb.invites_dao.InvitesDao
import com.example.eventmanagement.repository.roomDb.user_dao.UserDao
import com.example.eventmanagement.repository.worker.SyncRepository
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.utils.PreferencesUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userDataMethods: UserDataMethods,
    private val inviteMethods: InviteMethods,
    private val eventDataMethods: EventDataMethods,
    private val userDao: UserDao,
    private val eventDao: EventDao,
    private val invitesDao: InvitesDao,
    private val attendeeDao: AttendeeDao,
    private val favEventsDao: FavEventsDao,
    private val pendingOperationDao: PendingOperationDao,
    private val connectivityObserver: ConnectivityObserver,
    private val preferencesUtil: PreferencesUtil,
    private val syncRepository: SyncRepository
) : ViewModel(), ConnectivityObserver.ConnectivityListener {

    private val _currentUser = MutableStateFlow<User.UserData?>(null)
    val currentUser: StateFlow<User.UserData?> get() = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User.UserData>>(emptyList())
    val allUsers: StateFlow<List<User.UserData>> get() = _allUsers.asStateFlow()

    private val _allEvents = MutableStateFlow<List<EventData>>(emptyList())
    val allEvents: StateFlow<List<EventData>> get() = _allEvents.asStateFlow()

    private val _allFavEvents = MutableStateFlow<List<String>>(emptyList())
    val allFavEvents: StateFlow<List<String>> get() = _allFavEvents.asStateFlow()

    private val _allInvites = MutableStateFlow<List<Invites>>(emptyList())
    val allInvites: StateFlow<List<Invites>> get() = _allInvites.asStateFlow()

    private val _currentUserInvites = MutableStateFlow<List<EventInvites>>(emptyList())
    val currentUserInvites: StateFlow<List<EventInvites>> get() = _currentUserInvites.asStateFlow()

    private val _currentUserInvitedEvents = MutableStateFlow<List<EventData>>(emptyList())
    val currentUserInvitedEvents: StateFlow<List<EventData>> get() = _currentUserInvitedEvents.asStateFlow()

    private val _observeCurrentUserFromAttendees = MutableStateFlow<List<Attendees>>(emptyList())
    val observeCurrentUserFromAttendees: StateFlow<List<Attendees>> get() = _observeCurrentUserFromAttendees.asStateFlow()

    init {
        connectivityObserver.registerListener(this)
        initializeObservers()
    }

    override fun onConnectivityChanged(isConnected: Boolean) {
        if (isConnected) {
            initializeObservers()
        }
    }

    fun initializeObservers() {
        observePendingOperations()
        observeCurrentUser()
        observeAllUsers()
        observeAllEvents()
        observeAllInvites()
    }

    fun observePendingOperations() {
        viewModelScope.launch {
            pendingOperationDao.observePendingOperations()
                .collect {
                    syncRepository.scheduleSyncPendingOperationsWorker()
                }
        }
    }

    fun observeCurrentUser() {
        viewModelScope.launch {
            if (connectivityObserver.isConnected) {
                userDataMethods.observeCurrentUser { user ->
                    _currentUser.value = user
                    user?.let {
                        if (it.userRole == "Attendee") {
                            observeAllFavEvents(it.userId.toString())
                            observeCurrentUserFromAttendees(it.userId.toString())
                            observeUserInvites()
                        }
                    }
                }
            } else {
                userDao.observeCurrentUser(preferencesUtil.getUser()?.userId.toString()).collect {
                    _currentUser.value = it
                    observeAllFavEvents(it?.userId.toString())
                    observeCurrentUserFromAttendees(it?.userId.toString())
                    observeUserInvites()
                }

            }
        }
    }

    private fun observeAllInvites() {
        viewModelScope.launch {
            if (connectivityObserver.isConnected) {
                inviteMethods.observeAllInvites { invites ->
                    _allInvites.value = invites
                }
            } else {
                invitesDao.observeAllInvites().collect { invites ->
                    _allInvites.value = invites
                }
            }
        }
    }

    private fun observeCurrentUserFromAttendees(userId: String) {
        viewModelScope.launch {
            if (connectivityObserver.isConnected) {
                eventDataMethods.observeCurrentUserFromAttendees(userId) { data ->
                    _observeCurrentUserFromAttendees.value = data
                }
            } else {
                attendeeDao.observeCurrentUserFromAttendees(userId).collect { data ->
                    _observeCurrentUserFromAttendees.value = data
                }
            }
        }
    }

    private fun observeUserInvites() {
        viewModelScope.launch {
            if (connectivityObserver.isConnected) {
                inviteMethods.observeCurrentUserInvites { invites ->
                    updateUserInvitesAndEvents(invites)
                }
            } else {
                invitesDao.observeAllInvites().collect { invites ->
                    Log.d("VMEventInvites", "observeInviteEvents: $invites")
                    updateUserInvitesAndEvents(invites)
                }
            }
        }
    }

    private fun updateUserInvitesAndEvents(invites: List<Invites>) {
        val invitedEventsList = _allEvents.value.filter { event ->
            invites.any { invite -> invite.eventId == event.eventId }
        }
        _currentUserInvitedEvents.value = invitedEventsList
        _currentUserInvites.value = invites.mapNotNull { invite ->
            val event = invitedEventsList.firstOrNull { it.eventId == invite.eventId }
            val sender = _allUsers.value.firstOrNull { it.userId == invite.senderId }
            event?.let {
                sender?.let {
                    EventInvites(
                        inviteId = invite.inviteId,
                        eventId = event.eventId,
                        eventTitle = event.eventTitle,
                        eventOrganizer = sender.userName,
                        eventTiming = event.eventTiming,
                        eventDate = event.eventDate,
                        senderName = sender.userName,
                        inviteTime = invite.inviteTime,
                        inviteStatus = invite.inviteStatus
                    )
                }
            }
        }
    }


    private fun observeAllUsers() {
        viewModelScope.launch {
            if (connectivityObserver.isConnected) {
                userDataMethods.observeUsers { users ->
                    _allUsers.value = users.orEmpty()
                }
            } else {
                userDao.observeUsers().collect { users ->
                    if (users != null) {
                        _allUsers.value = users
                    }
                }
            }
        }
    }

    private fun observeAllEvents() {
        viewModelScope.launch {
            if (connectivityObserver.isConnected) {
                eventDataMethods.observeAllEvents { events ->
                    _allEvents.value = events
                }
            } else {
                eventDao.observeAllEvents().collect { events ->
                    _allEvents.value = events
                }
            }
        }
    }

    private fun observeAllFavEvents(userId: String) {
        if (connectivityObserver.isConnected) {
            viewModelScope.launch {
                eventDataMethods.observeCurrentUserFavEvents(userId) { events ->
                    _allFavEvents.value = events
                }
            }
        } else {
            viewModelScope.launch {
                favEventsDao.observeCurrentUserFavEvents(userId).collect { result ->
                    Log.d("ObservingFavEvents", "observeCurrUserFavEvents: $result")
                    _allFavEvents.update { result }
                }
            }
        }
    }

    fun getProfileStatus(): String {
        return if (currentUser.value?.profilePrivate == true) "Public" else "Private"
    }


    fun resetViewModel() {
        _currentUser.value = null
        _allUsers.value = emptyList()
        _allEvents.value = emptyList()
    }
}
