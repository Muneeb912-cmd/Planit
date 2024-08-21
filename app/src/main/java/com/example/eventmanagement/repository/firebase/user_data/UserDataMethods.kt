package com.example.eventmanagement.repository.firebase.user_data

import com.example.eventmanagement.models.User

interface UserDataMethods {
    suspend fun getAllUserData(): List<User.UserData>
    fun getCurrentUser(): User.UserData?
    suspend fun getUserDataFromFireStore(
        userId: String,
        onResult: (Boolean, User.UserData?) -> Unit
    )
    suspend fun updateUserLocation(userId:String,newLocation:String,onResult: (Boolean) -> Unit)
    suspend fun updateUserNotificationSettings(userId:String,newSettings:Boolean,onResult: (Boolean) -> Unit)
    suspend fun updateUserProfileStatus(userId:String,newSettings: Boolean,onResult: (Boolean) -> Unit)
    suspend fun updateUserProfile(userId: String,userName:String,userEmail:String,userPhone:String,userDob:String,userImg:String,onResult: (Boolean) -> Unit)
    fun observeCurrentUser(onResult: (User.UserData?) -> Unit)
    fun observeUsers(onResult: (List<User.UserData>?) -> Unit)
}