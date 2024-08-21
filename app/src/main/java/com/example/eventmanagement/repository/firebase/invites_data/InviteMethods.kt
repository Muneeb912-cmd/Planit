package com.example.eventmanagement.repository.firebase.invites_data

import com.example.eventmanagement.models.EventsInvites

interface InviteMethods {
    fun getInvitesBySender(senderId:String):List<EventsInvites>
    fun getInvitesByReceiver(receiverId:String):List<EventsInvites>
    fun createInvite(invite:EventsInvites,onResult: (Boolean)->Unit)
    fun observeCurrentUserInvites(onResult: (List<EventsInvites>) -> Unit)
    fun deleteInvite(inviteId:String,onResult: (Boolean) -> Unit)
    fun updateInviteStatus(inviteId: String, newStatus: String, onResult: (Boolean) -> Unit)
}