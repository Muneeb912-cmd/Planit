package com.example.eventmanagement.workers.sync_data_from_firebase

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.room_db.LocalDB
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncUserDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    val firestore = FirebaseFirestore.getInstance()
    val database = LocalDB.getInstance(context)
    private val userDao = database.userDao()
    private val connectivityObserver = ConnectivityObserver(context)
    override fun doWork(): Result {
        return try {
            if (!connectivityObserver.isConnected) {
                return Result.retry()
            }
            runBlocking {
                val users = fetchUsersFromFirestore()
                syncUsers(users)
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private suspend fun fetchUsersFromFirestore(): List<User.UserData> = withContext(Dispatchers.IO) {
        try {
            val users = mutableListOf<User.UserData>()
            val querySnapshot = firestore.collection("UserData").get().await()
            for (document in querySnapshot.documents) {
                val user = document.toObject(User.UserData::class.java)
                user?.let { users.add(it) }
            }
            users
        } catch (e: Exception) {
            Log.e("Worker", "Error fetching users", e)
            emptyList()
        }
    }

    private suspend fun syncUsers(users: List<User.UserData>) {
        withContext(Dispatchers.IO) {
            val existingUsers = userDao.getAllUserData()

            // Determine users to update
            val usersToUpdate = existingUsers.mapNotNull { existingUser ->
                val newUser = users.find { it.userId == existingUser.userId }
                if (newUser != null && !areUsersEqual(existingUser, newUser)) {
                    existingUser.apply {
                        userName = newUser.userName
                        userEmail = newUser.userEmail
                        userPhone = newUser.userPhone
                        userDob = newUser.userDob
                        userPassword = newUser.userPassword
                        userRole = newUser.userRole
                        userImg = newUser.userImg
                        userLocation = newUser.userLocation
                        userLoginType = newUser.userLoginType
                        notificationsAllowed = newUser.notificationsAllowed
                        profilePrivate = newUser.profilePrivate
                        userBanned = newUser.userBanned
                    }
                } else {
                    null
                }
            }

            // Determine users to insert
            val usersToInsert = users.filter { newUser ->
                existingUsers.none { it.userId == newUser.userId }
            }

            // Determine users to delete
            val userIdsFromFirestore = users.map { it.userId }.toSet()
            val usersToDelete = existingUsers.filter { it.userId !in userIdsFromFirestore }

            // Update users
            for (user in usersToUpdate) {
                Log.d("UserWorker", "Updating user: $user")
                userDao.updateUserProfile(
                    user.userId.toString(),
                    user.userName.toString(),
                    user.userPhone.toString(),
                    user.userDob.toString(),
                    user.userImg.toString()
                )
            }

            // Insert new users
            for (user in usersToInsert) {
                Log.d("UserWorker", "Inserting user: $user")
                userDao.saveUserData(user)
            }

            // Delete users no longer in Firestore
            for (user in usersToDelete) {
                Log.d("UserWorker", "Deleting user: $user")
                userDao.deleteUser(user.userId.toString())
            }
        }
    }


    private fun areUsersEqual(existingUser: User.UserData, newUser: User.UserData): Boolean {
        return existingUser.userName == newUser.userName &&
                existingUser.userEmail == newUser.userEmail &&
                existingUser.userPhone == newUser.userPhone &&
                existingUser.userDob == newUser.userDob &&
                existingUser.userPassword == newUser.userPassword &&
                existingUser.userRole == newUser.userRole &&
                existingUser.userImg == newUser.userImg &&
                existingUser.userLocation == newUser.userLocation &&
                existingUser.userLoginType == newUser.userLoginType &&
                existingUser.notificationsAllowed == newUser.notificationsAllowed &&
                existingUser.profilePrivate == newUser.profilePrivate &&
                existingUser.userBanned == newUser.userBanned
    }

}
