package com.example.eventmanagement.repository.firebase.login_signup

import android.net.Uri
import com.example.eventmanagement.models.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

import org.mockito.Mockito
import org.mockito.Mockito.mockStatic

class LoginSignUpMethodsImplTest {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var collectionReference: CollectionReference
    private lateinit var loginSignUpMethods: LoginSignUpMethodsImpl
    private var storageRef = mock<StorageReference>()
    @BeforeEach
    fun setUp() {
        firestore = Mockito.mock()
        collectionReference = Mockito.mock()
        firebaseStorage=mock()
        auth = mock()
        whenever(firestore.collection("UserData")).thenReturn(collectionReference)
        whenever(firebaseStorage.reference).thenReturn(storageRef)
        loginSignUpMethods = LoginSignUpMethodsImpl(auth, firestore, mock())

        // Mock static methods
        mockStatic(Uri::class.java).use { mockedUri ->
            mockedUri.`when`<Uri> { Uri.parse(any()) }.thenAnswer {
                mock<Uri>()
            }
        }
    }


    @Test
    fun signOut_success() {
        // Arrange
        doNothing().whenever(auth).signOut()

        // Act
        var result: Boolean? = null
        loginSignUpMethods.signOut { success ->
            result = success
        }

        // Assert
        assertTrue(result ?: false)
    }
    @Test
    fun sendResetPasswordEmail_success() {
        // Arrange
        val email = "test@example.com"
        val task: Task<Void> = Tasks.forResult(null) // Simulate a successful Task

        whenever(auth.sendPasswordResetEmail(email)).thenReturn(task)

        // Act
        var result: Boolean? = null
        loginSignUpMethods.sendResetPasswordEmail(email) { success ->
            result = success
        }

        // Assert
        assertTrue(result ?: false)
    }

}