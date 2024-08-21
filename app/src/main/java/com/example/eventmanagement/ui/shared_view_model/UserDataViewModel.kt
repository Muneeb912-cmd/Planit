package com.example.eventmanagement.ui.shared_view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.EventsInvites
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.envents_data.EventDataMethods
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDataViewModel @Inject constructor(
    private val userDataMethods: UserDataMethods,
    private val inviteMethods: InviteMethods,
    private val eventDataMethods: EventDataMethods
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User.UserData?>(null)
    val currentUser: StateFlow<User.UserData?> get() = _currentUser.asStateFlow()

    private val _userInvites = MutableStateFlow<List<EventsInvites>>(emptyList())
    val userInvites: StateFlow<List<EventsInvites>> get() = _userInvites.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User.UserData>>(emptyList())
    val allUsers: StateFlow<List<User.UserData>> get() = _allUsers.asStateFlow()

    private val _allEvents = MutableStateFlow<List<EventData>>(emptyList())
    val allEvents: StateFlow<List<EventData>> get() = _allEvents.asStateFlow()

    init {
        observeCurrentUser()
        observeUserInvites()
        observeAllUsers()
        observeAllEvents()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userDataMethods.observeCurrentUser { user ->
                _currentUser.value = user
            }
        }
        Log.d("observeCurrentUser", "observeCurrentUser: ${currentUser.value}")
    }

    private fun observeUserInvites() {
        viewModelScope.launch {
            val receiverId = _currentUser.value?.userId
            receiverId?.let {
                inviteMethods.observeCurrentUserInvites { invites ->
                    _userInvites.value = invites
                }
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


    fun getProfileStatus(): String {
        return if (currentUser.value?.isProfilePrivate == true) {
            "Public"
        } else {
            "Private"
        }
    }
}
