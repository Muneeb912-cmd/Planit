package com.example.eventmanagement.ui.activities.manage_invites

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import com.example.eventmanagement.utils.Response
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageEventsViewModel @Inject constructor(
    private val inviteMethods: InviteMethods
) : ViewModel() {

    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val states: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun createInvite(
        eventId: String,
        currentUserId: String,
        userData: User.UserData,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val invite = Invites(
                eventId = eventId,
                inviteStatus = "pending",
                senderId = currentUserId,
                receiverId = userData.userId,
                inviteTime = Timestamp.now()
            )
            inviteMethods.createInvite(invite) { result ->
                if (result) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
        }
    }

    fun deleteInvite(eventId: String, userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            inviteMethods.deleteInvite(eventId, userId) { result ->
                if (result) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
        }
    }

}