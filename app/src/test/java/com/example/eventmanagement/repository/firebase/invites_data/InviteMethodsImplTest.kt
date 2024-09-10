package com.example.eventmanagement.repository.firebase.invites_data

import com.example.eventmanagement.models.Invites
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InviteMethodsImplTest {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var eventsDataMethods: InviteMethodsImpl
    private lateinit var collectionReference: CollectionReference

    @BeforeEach
    fun setUp() {
        auth = mock()
        firestore = mock()
        collectionReference = mock()
        whenever(firestore.collection("Events")).thenReturn(collectionReference)
        eventsDataMethods = InviteMethodsImpl(auth, firestore)
    }
    @Test
    fun createInvite() {
        // Arrange
        val invite = Invites(inviteId = "", eventId = "event_123", receiverId = "user_123", inviteStatus = "Pending")
        val mockCollectionReference = mock<CollectionReference>()
        val mockDocumentReference = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()

        // Mock Firestore collection and document reference
        whenever(firestore.collection("Invites")).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document()).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.id).thenReturn("generated_invite_id") // Mock the ID generation
        whenever(mockDocumentReference.set(invite)).thenReturn(mockTask)

        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var resultSuccess = false
        eventsDataMethods.createInvite(invite) { success ->
            resultSuccess = success
        }

        // Assert
        assertTrue(resultSuccess)
        assertEquals("generated_invite_id", invite.inviteId) // Ensure inviteId is set correctly
    }

}