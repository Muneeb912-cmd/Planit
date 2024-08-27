package com.example.eventmanagement.repository.firebase.invites_data

import android.util.Log
import com.example.eventmanagement.models.Invites
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class InviteMethodsImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : InviteMethods {

    private var invitesListener: ListenerRegistration? = null

    override fun createInvite(
        receiverId: String,
        invite: Invites,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("UserData").document(receiverId).collection("Invites")
            .add(invite)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun observeCurrentUserInvites(onResult: (List<Invites>) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            invitesListener = firestore.collection("UserData")
                .document(currentUserId)
                .collection("Invites")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.d("events", "observeCurrentUserInvites: $e")
                        onResult(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        val invites = snapshots.toObjects(Invites::class.java)
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
                .collection("Invites")
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

    override fun updateInviteStatus(
        inviteId: String,
        newStatus: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            firestore.collection("UserData")
                .document(currentUserId)
                .collection("Invites")
                .document(inviteId)
                .update("inviteStatus", newStatus)
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
