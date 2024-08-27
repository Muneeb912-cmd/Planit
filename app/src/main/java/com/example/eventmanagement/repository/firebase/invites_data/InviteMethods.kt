package com.example.eventmanagement.repository.firebase.invites_data

import com.example.eventmanagement.models.Invites

interface InviteMethods {
    fun createInvite(
        receiverId: String,
        invite: Invites,
        onResult: (Boolean) -> Unit
    )

    fun observeCurrentUserInvites(onResult: (List<Invites>) -> Unit)
    fun deleteInvite(inviteId: String, onResult: (Boolean) -> Unit)
    fun updateInviteStatus(inviteId: String, newStatus: String, onResult: (Boolean) -> Unit)
}