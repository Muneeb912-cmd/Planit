package com.example.eventmanagement.repository.firebase.user_data

import android.util.Log
import com.example.eventmanagement.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserDataImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
):UserDataMethods {
    override suspend fun getAllUserData(): List<User.UserData> {
        return try {
            val querySnapshot = firestore.collection("UserData").get().await()
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(User.UserData::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getCurrentUser(): User.UserData? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { user ->
            User.UserData(
                userId = user.uid,
                userEmail = user.email,
                userName = user.displayName,
                userImg=user.photoUrl.toString(),
                userPhone = user.phoneNumber
            )
        }
    }

    override suspend fun getUserDataFromFireStore(
        userId: String,
        onResult: (Boolean, User.UserData?) -> Unit
    ) {
        try {
            val documentSnapshot = firestore.collection("UserData")
                .document(userId)
                .get()
                .await()

            val userData = documentSnapshot.toObject(User.UserData::class.java)

            if (userData != null) {
                onResult(true, userData)
            } else {
                onResult(false, null)
            }
        } catch (e: Exception) {
            onResult(false, null)
        }
    }


}