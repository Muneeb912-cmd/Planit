package com.example.eventmanagement.repository.firebase.user_data

import android.net.Uri
import android.util.Log
import com.example.eventmanagement.models.User
import com.example.eventmanagement.utils.PreferencesUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserDataImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val preferences: PreferencesUtil
) : UserDataMethods {
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
                userImg = user.photoUrl.toString(),
                userPhone = user.phoneNumber
            )
        }
    }

    override suspend fun getUserDataFromFireStore(
        userId: String,
        onResult: (Boolean, User.UserData?,String) -> Unit
    ) {
        try {
            val documentSnapshot = firestore.collection("UserData")
                .document(userId)
                .get()
                .await()

            val userData = documentSnapshot.toObject(User.UserData::class.java)

            if (userData != null) {
                onResult(true, userData,"Good")
            } else {
                onResult(false, null,"Data Null")
            }
        } catch (e: Exception) {
            onResult(false, null,e.toString())
        }
    }

    override suspend fun updateUserLocation(
        userId: String,
        newLocation: String,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val updateData = mapOf("userLocation" to newLocation)
            firestore.collection("UserData")
                .document(userId)
                .update(updateData)
                .await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }


    override suspend fun updateUserNotificationSettings(
        userId: String,
        newSettings: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val updateData = mapOf("notificationsAllowed" to newSettings)
            firestore.collection("UserData")
                .document(userId)
                .update(updateData)
                .await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }


    override suspend fun updateUserProfileStatus(
        userId: String,
        newSettings: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val updateData = mapOf("profilePrivate" to newSettings)
            firestore.collection("UserData")
                .document(userId)
                .update(updateData)
                .await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        userName: String,
        userPhone: String,
        userDob: String,
        userImg: String,
        onResult: (Boolean) -> Unit
    ) {
        try {
            // Fetch current user data
            val userDoc = firestore.collection("UserData").document(userId).get().await()
            val currentUserData = userDoc.data ?: emptyMap<String, Any>()
            val currentUserImg = currentUserData["userImg"] as? String

            // Prepare updates
            val updates = mutableMapOf<String, Any>(
                "userName" to userName,
                "userPhone" to userPhone,
                "userDob" to userDob
            )

            // Check if the image needs to be updated
            if (userImg.isNotEmpty() && userImg != currentUserImg) {
                val storageRef = firebaseStorage.reference
                val userImgRef = storageRef.child("ProfileImages/$userId")
                userImgRef.putFile(Uri.parse(userImg)).await()
                val downloadUrl = userImgRef.downloadUrl.await()
                updates["userImg"] = downloadUrl.toString()
            } else if (userImg.isEmpty() && currentUserImg != null) {
                // Remove userImg field if userImg is empty and it exists in the current data
                updates.remove("userImg")
            }

            // Update Firestore
            firestore.collection("UserData")
                .document(userId)
                .update(updates)
                .await()

            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }


    private var currentUserListener: ListenerRegistration? = null
    private var usersListener: ListenerRegistration? = null

    override fun observeCurrentUser(onResult: (User.UserData?) -> Unit) {
        var currentUserId = ""
        currentUserId = if (preferences.getUser() != null) {
            preferences.getUser()!!.userId.toString()
        } else {
            getCurrentUser()?.userId.toString()
        }
        Log.d("observeCurrentUser", "UserID: $currentUserId")
        currentUserListener = firestore.collection("UserData")
            .document(currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onResult(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val userData = snapshot.toObject(User.UserData::class.java)
                    if (userData != null) {
                        preferences.updateUser(userData)
                    }
                    onResult(userData)
                } else {
                    onResult(null)
                }
            }
    }

    override fun observeUsers(onResult: (List<User.UserData>?) -> Unit) {
        usersListener = firestore.collection("UserData")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onResult(null)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val users = snapshots.toObjects(User.UserData::class.java)
                    onResult(users)
                } else {
                    onResult(emptyList())
                }
            }
    }

    override fun removeCurrentUserListener() {
        currentUserListener?.remove()
    }

    override suspend fun updateUserBanStatus(userId: String, banStatus:Boolean, onResult: (Boolean) -> Unit) {
        try {
            val updateData = mapOf("userBanned" to banStatus)
            firestore.collection("UserData")
                .document(userId)
                .update(updateData)
                .await()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    fun removeUsersListener() {
        usersListener?.remove()
    }


}