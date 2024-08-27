package com.example.eventmanagement.ui.activities.event_invites

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventInviteViewModel @Inject constructor(
    private val inviteMethods: InviteMethods
) :ViewModel() {

    fun updateInviteStatus(inviteId:String,newStatus:String, onResult: (Boolean,String)->Unit){
        inviteMethods.updateInviteStatus(inviteId,newStatus){result->
            if(result){
                onResult(true,"All Good!")
            }else{
                onResult(false,"Couldn't update invite status!")
            }
        }
    }
}