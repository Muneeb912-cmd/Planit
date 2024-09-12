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
        invite: Invites,
        onResult: (Boolean) -> Unit
    ) {
        val inviteDocRef = firestore.collection("Invites").document()
        invite.inviteId = inviteDocRef.id // Set inviteId to document ID

        inviteDocRef.set(invite)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun observeAllInvites(onResult: (List<Invites>) -> Unit) {
        invitesListener = firestore.collection("Invites")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.d("events", "observeAllInvites: $e")
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
    }

    override fun observeCurrentUserInvites(onResult: (List<Invites>) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {
            invitesListener = firestore.collection("Invites")
                .whereEqualTo("receiverId", currentUserId)
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

    override fun deleteInvite(eventId: String, receiverId: String, onResult: (Boolean) -> Unit) {
        val invitesRef = firestore.collection("Invites")

        // Query to find the document with the matching eventId and userId
        invitesRef
            .whereEqualTo("eventId", eventId)
            .whereEqualTo("receiverId", receiverId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onResult(false)
                    return@addOnSuccessListener
                }
                for (document in querySnapshot.documents) {
                    // Delete the document
                    invitesRef.document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            onResult(true)
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }


    override fun updateInviteStatus(
        inviteId: String,
        newStatus: String,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("Invites")
            .document(inviteId)
            .update("inviteStatus", newStatus)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun updateInviteStatusByUserId(
        userId: String,
        eventId: String,
        newStatus: String,
        onResult: (Boolean) -> Unit
    ) {
        firestore.collection("Invites")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming there's only one matching document
                    val document = querySnapshot.documents[0]
                    firestore.collection("Invites")
                        .document(document.id)
                        .update("inviteStatus", newStatus)
                        .addOnSuccessListener {
                            onResult(true)
                        }
                        .addOnFailureListener {
                            onResult(false)
                        }
                } else {
                    onResult(false) // No matching document found
                }
            }
            .addOnFailureListener {
                onResult(false) // Query failed
            }
    }

}
