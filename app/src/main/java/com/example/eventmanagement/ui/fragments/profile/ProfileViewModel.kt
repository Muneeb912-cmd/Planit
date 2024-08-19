package com.example.eventmanagement.ui.fragments.profile

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel@Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods,
    private val preferencesUtil: PreferencesUtil
) : ViewModel()  {

    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val profileStates: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    fun signOut() {
        _states.value = Response.Loading
        try {
            loginSignUpMethods.signOut { userSignedOut ->
                if (userSignedOut) {
                    _states.value = Response.Success(Unit)
                    preferencesUtil.deleteUser()
                } else {
                    _states.value = Response.Error(Exception("Failed to sign out"))
                }
            }
        } catch (e: Exception) {
            _states.value = Response.Error(e)
        }
    }

}