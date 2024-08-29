package com.example.eventmanagement.ui.shared_view_model

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val userDataMethods: UserDataMethods,
    private val inviteMethods: InviteMethods,
    private val eventDataMethods: EventDataMethods
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User.UserData?>(null)
    val currentUser: StateFlow<User.UserData?> get() = _currentUser.asStateFlow()

    private val _userInvites = MutableStateFlow<List<Invites>>(emptyList())
    val userInvites: StateFlow<List<Invites>> get() = _userInvites.asStateFlow()

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
        observeCurrentUser()
        observeAllUsers()
        observeAllEvents()
        observeAllInvites()
        observeUserInvites()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userDataMethods.observeCurrentUser { user ->
                _currentUser.value = user
                user?.let {
                    if (it.userRole == "Attendee") {
                        observeAllFavEvents(it.userId.toString())
                        observeCurrentUserFromAttendees(it.userId.toString())
                    }
                }
            }
        }
    }

    private fun observeAllInvites() {
        viewModelScope.launch {
            inviteMethods.observeAllInvites { invites ->
                _allInvites.value = invites
            }
        }
    }

    private fun observeCurrentUserFromAttendees(userId:String){
        viewModelScope.launch {
            eventDataMethods.observeCurrentUserFromAttendees(
              userId
            ) {data ->
                _observeCurrentUserFromAttendees.value = data
            }
        }
    }

    private fun observeUserInvites() {
        viewModelScope.launch {
            inviteMethods.observeCurrentUserInvites { invites ->
                val invitedEventsList = _allEvents.value.filter { event ->
                    invites.any { invite -> invite.eventId == event.eventId }
                }
                _currentUserInvitedEvents.value = invitedEventsList
                val eventInvitesList = invites.mapNotNull { invite ->
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
                _currentUserInvites.value = eventInvitesList
            }
        }
    }


    private fun observeAllUsers() {
        viewModelScope.launch {
            userDataMethods.observeUsers { users ->
                _allUsers.value = users.orEmpty()
            }
        }
    }

    private fun observeAllEvents() {
        viewModelScope.launch {
            eventDataMethods.observeAllEvents { events ->
                _allEvents.value = events
            }
        }
    }

    private fun observeAllFavEvents(currentUserID: String) {
        if (currentUser.value?.userRole == "Attendee") {
            viewModelScope.launch {
                eventDataMethods.observeCurrentUserFavEvents(currentUserID) { events ->
                    _allFavEvents.value = events
                }
            }
        }
    }

    fun getProfileStatus(): String {
        return if (currentUser.value?.isProfilePrivate == true) {
            "Public"
        } else {
            "Private"
        }
    }

    fun resetViewModel() {
        _currentUser.value = null
        _userInvites.value = emptyList()
        _allUsers.value = emptyList()
        _allEvents.value = emptyList()
    }

    fun initializeObservers() {
        observeCurrentUser()
        observeAllUsers()
        observeUserInvites()
        observeAllEvents()
    }
}
