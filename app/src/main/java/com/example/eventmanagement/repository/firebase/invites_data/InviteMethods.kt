package com.example.eventmanagement.repository.firebase.invites_data

import com.example.eventmanagement.models.Invites

interface InviteMethods {
    fun createInvite(
        invite: Invites,
        onResult: (Boolean) -> Unit
    )

    fun observeAllInvites(onResult: (List<Invites>) -> Unit)

    fun observeCurrentUserInvites(onResult: (List<Invites>) -> Unit)
    fun deleteInvite(eventId: String, userId:String, onResult: (Boolean) -> Unit)
    fun updateInviteStatus(inviteId: String, newStatus: String, onResult: (Boolean) -> Unit)
}