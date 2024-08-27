package com.example.eventmanagement.ui.shared_view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _allFavEvents = MutableStateFlow<List<EventData>>(emptyList())
    val allFavEvents: StateFlow<List<EventData>> get() = _allFavEvents.asStateFlow()

    private val _invitedEvents = MutableStateFlow<List<EventData>>(emptyList())
    val invitedEvents: StateFlow<List<EventData>> get() = _invitedEvents.asStateFlow()

    private val _eventInvites = MutableStateFlow<List<EventInvites>>(emptyList())
    val eventInvites: StateFlow<List<EventInvites>> get() = _eventInvites.asStateFlow()



    init {
        observeCurrentUser()
        observeAllEvents()
        observeAllUsers()
        observeUserInvites()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userDataMethods.observeCurrentUser { user ->
                _currentUser.value = user
                if (user != null) {
                    if (user.userRole == "Attendee") {
                        observeAllDFavEvents(user.userId.toString())
                    }
                }
            }
        }
    }

    private fun observeUserInvites() {
        viewModelScope.launch {
            inviteMethods.observeCurrentUserInvites { invites ->
                val filteredInvites = invites.filter { invite ->
                    _allEvents.value.any { event -> event.eventId == invite.eventId }
                }
                _userInvites.value = filteredInvites

                val invitedEventsList = _allEvents.value.filter { event ->
                    filteredInvites.any { invite -> invite.eventId == event.eventId }
                }
                _invitedEvents.value = invitedEventsList

                val eventInvitesList = filteredInvites.mapNotNull { invite ->
                    val event = _allEvents.value.firstOrNull { it.eventId == invite.eventId }
                    val sender = _allUsers.value.firstOrNull { it.userId == invite.senderId }

                    if (event != null && sender != null) {
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
                    } else {
                        null
                    }
                }

                _eventInvites.value = eventInvitesList
            }
        }
    }



    private fun observeAllUsers() {
        viewModelScope.launch {
            userDataMethods.observeUsers { users ->
                if (users != null) {
                    _allUsers.value = users
                }
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

    fun observeAllDFavEvents(currentUserID: String) {
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
        observeUserInvites()
        observeAllUsers()
        observeAllEvents()
    }

}
