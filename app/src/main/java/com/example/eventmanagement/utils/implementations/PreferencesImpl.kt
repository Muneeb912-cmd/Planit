package com.example.eventmanagement.utils.implementations

import android.content.Context
import android.content.SharedPreferences
import com.example.eventmanagement.models.User
import com.example.eventmanagement.utils.PreferencesUtil
import com.google.gson.Gson


class PreferencesImpl(context: Context): PreferencesUtil {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    override fun saveUser(user: User.UserData){
        val gson = Gson()
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString("user", userJson).apply()
    }


    override fun getUser(): User.UserData? {
        val userJson = sharedPreferences.getString("user", null)
        val gson = Gson()
        val user = if (userJson != null) {
            gson.fromJson(userJson, User.UserData::class.java)
        } else {
            null
        }
        return user
    }

    override fun updateUser(updater: (User.UserData) -> User.UserData) {
        val currentUser = getUser()
        if (currentUser != null) {
            val updatedUser = updater(currentUser)
            saveUser(updatedUser)
        }
    }
    override fun deleteUser() {
        sharedPreferences.edit().remove("user").apply()
    }
}