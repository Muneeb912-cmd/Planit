package com.example.eventmanagement.utils

import com.example.eventmanagement.models.User

interface PreferencesUtil {
    fun saveUser(user: User.UserData)
    fun getUser(): User.UserData?
    fun updateUser(updater: (User.UserData) -> User.UserData)
    fun deleteUser()
}