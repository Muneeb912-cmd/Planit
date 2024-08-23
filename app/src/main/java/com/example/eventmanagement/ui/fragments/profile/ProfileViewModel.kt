package com.example.eventmanagement.ui.fragments.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods,
    private val preferencesUtil: PreferencesUtil,
    private val userDataMethods: UserDataMethods
) : ViewModel() {

    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val profileStates: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    fun signOut() {
        _states.value = Response.Loading
        try {
            loginSignUpMethods.signOut { userSignedOut ->
                if (userSignedOut) {
                    _states.value = Response.Success(Unit)
                    preferencesUtil.deleteUser()
                    userDataMethods.removeCurrentUserListener()
                } else {
                    _states.value = Response.Error(Exception("Failed to sign out"))
                }
            }
        } catch (e: Exception) {
            _states.value = Response.Error(e)
        }
    }

    fun updateUserLocation(userId: String, newLocation: String,onResult: (Boolean)->Unit) {
        viewModelScope.launch {
            try {
                userDataMethods.updateUserLocation(userId, newLocation) { result ->
                    if (result) {
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateUserProfileStatus(userId: String, newSetting: Boolean,onResult: (Boolean)->Unit) {
        viewModelScope.launch {
            try {
                userDataMethods.updateUserProfileStatus(userId, newSetting) { result ->
                    if (result) {
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
              onResult(false)
            }
        }
    }

    fun updateUserNotificationStatus(userId: String, newSetting: Boolean,onResult: (Boolean)->Unit) {
        viewModelScope.launch {
            try {
                userDataMethods.updateUserNotificationSettings(userId, newSetting) { result ->
                    if (result) {
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}