package com.example.eventmanagement.models

data class UserUpdate(
    val userId: String? = null,
    val userName: String? = null,
    val userPhone: String? = null,
    val userDob: String? = null,
    val userImg: String? = null
)
