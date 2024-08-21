package com.example.eventmanagement.repository.firebase.invites_data

import com.example.eventmanagement.models.EventsInvites
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class InviteMethodsImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : InviteMethods {

    private var invitesListener: ListenerRegistration? = null

    override fun getInvitesBySender(senderId: String): List<EventsInvites> {
        val invitesList = mutableListOf<EventsInvites>()
        firestore.collection("Invites")
            .whereEqualTo("senderId", senderId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val invites = snapshots.toObjects(EventsInvites::class.java)
                    invitesList.addAll(invites)
                }
            }
            .addOnFailureListener {
                // Handle the error
            }
        return invitesList
    }

    override fun getInvitesByReceiver(receiverId: String): List<EventsInvites> {
        val invitesList = mutableListOf<EventsInvites>()
        firestore.collection("Invites")
            .whereEqualTo("receiverId", receiverId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val invites = snapshots.toObjects(EventsInvites::class.java)
                    invitesList.addAll(invites)
                }
            }
            .addOnFailureListener {
                // Handle the error
            }
        return invitesList
    }

    override fun createInvite(invite: EventsInvites, onResult: (Boolean) -> Unit) {
        firestore.collection("Invites")
            .add(invite)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun observeCurrentUserInvites(onResult: (List<EventsInvites>) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            invitesListener = firestore.collection("UserData")
                .document(currentUserId)
                .collection("MyInvites")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        onResult(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val invites = snapshots.toObjects(EventsInvites::class.java)
                        onResult(invites)
                    } else {
                        onResult(emptyList())
                    }
                }
        } else {
            onResult(emptyList())
        }
    }

    override fun deleteInvite(inviteId: String, onResult: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestore.collection("UserData")
                .document(currentUserId)
                .collection("MyInvites")
                .document(inviteId)
                .delete()
                .addOnSuccessListener {
                    onResult(true)
                }
                .addOnFailureListener {
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }

    override fun updateInviteStatus(inviteId: String, newStatus: String, onResult: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestore.collection("UserData")
                .document(currentUserId)
                .collection("Invites")
                .document(inviteId)
                .update("status", newStatus)
                .addOnSuccessListener {
                    onResult(true)
                }
                .addOnFailureListener {
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }


    fun removeInvitesListener() {
        invitesListener?.remove()
    }
}
