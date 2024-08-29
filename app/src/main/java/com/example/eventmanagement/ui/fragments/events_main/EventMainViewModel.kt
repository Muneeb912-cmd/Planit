package com.example.eventmanagement.ui.fragments.events_main

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventMainViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods,
    private val preferencesUtil: PreferencesUtil,
    private val userDataMethods: UserDataMethods
)  : ViewModel() {

    fun signOut(onResult: (Boolean, String)->Unit) {
        try {
            loginSignUpMethods.signOut { userSignedOut ->
                if (userSignedOut) {
                    preferencesUtil.deleteUser()
                    userDataMethods.removeCurrentUserListener()
                    onResult(true,"User Signed Out")
                }
            }
        } catch (e: Exception) {
            onResult(false,"Error Signing Out User")
        }
    }
}