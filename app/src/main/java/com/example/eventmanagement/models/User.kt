package com.example.eventmanagement.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

sealed class User {
    @Entity(
        tableName = "users",
        indices = [Index(value = ["userId"], unique = true)]
    )
    data class UserData(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        var userId: String? = null,
        var userName: String? = null,
        var userEmail: String? = null,
        var userPhone: String? = null,
        var userDob: String? = null,
        var userPassword: String? = null,
        var userRole: String? = null,
        var userImg: String? = null,
        var userLocation: String? = null,
        var userLoginType: String? = null,
        var notificationsAllowed: Boolean? = null,
        var profilePrivate: Boolean? = null,
        var userBanned: Boolean? = null
    )
}