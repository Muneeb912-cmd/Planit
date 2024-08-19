package com.example.eventmanagement.repository.firebase.user_data

import com.example.eventmanagement.models.User

interface UserDataMethods {
    suspend fun getAllUserData(): List<User.UserData>
    fun getCurrentUser(): User.UserData?
    suspend fun getUserDataFromFireStore(
        userId: String,
        onResult: (Boolean, User.UserData?) -> Unit
    )
}