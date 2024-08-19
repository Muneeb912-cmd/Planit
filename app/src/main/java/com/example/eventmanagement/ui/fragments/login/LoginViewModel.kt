package com.example.eventmanagement.ui.fragments.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods,
    private val userDataMethods: UserDataMethods,
    private val preferencesUtil: PreferencesUtil
) : ViewModel() {

    private val _loginResult = MutableStateFlow<Response<Unit>>(Response.Loading)
    val loginResult: StateFlow<Response<Unit>> get() = _loginResult.asStateFlow()

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> get() = _isEmailVerified.asStateFlow()

    private val _usersData = MutableStateFlow<Response<List<User.UserData>>>(Response.Loading)
    val usersData: StateFlow<Response<List<User.UserData>>> get() = _usersData.asStateFlow()

    fun signInWithEmailPassword(email: String, password: String) {
        _loginResult.value = Response.Loading
        loginSignUpMethods.signInWithEmailPassword(email, password) { isVerified, msg ->
            if (isVerified) {
                checkEmailVerification()
                getAllUsers()
            } else {
                _loginResult.value = Response.Error(java.lang.Exception(msg))
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        _loginResult.value = Response.Loading
        loginSignUpMethods.signInWithGoogle(account) { isVerified ->
            if (isVerified) {
                checkEmailVerification()
                getAllUsers()
            } else {
                _loginResult.value = Response.Error(Exception("User Don't Exist"))
            }
        }
    }

    private fun checkEmailVerification() {
        loginSignUpMethods.checkEmailVerification { isVerified ->
            _isEmailVerified.value = isVerified
            _loginResult.value = if (isVerified) {
                Response.Success(Unit)
            } else {
                Response.Error(Exception("Email not verified"))
            }
        }
    }

    private fun getAllUsers() {
        _usersData.value = Response.Loading
        viewModelScope.launch {
            try {
                val users = userDataMethods.getAllUserData()
                _usersData.value = Response.Success(users)
            } catch (e: Exception) {
                _usersData.value = Response.Error(e)
            }
        }
    }

    fun getCurrentUser(): User.UserData? {
        val firebaseUser = userDataMethods.getCurrentUser()
        return firebaseUser?.let { user ->
            User.UserData(
                userId = user.userId,
                userEmail = user.userEmail,
            )
        }
    }

    fun getUserDataFromFireStore(userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userDataMethods.getUserDataFromFireStore(userId) { success, userData ->
                    if (success && userData != null) {
                        saveDataToPreferences(userData) { isUserSavedToPreferences ->
                            onResult(isUserSavedToPreferences)
                        }
                    } else {
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                // Optionally log the exception
                onResult(false)
            }
        }
    }

    private fun saveDataToPreferences(userData: User.UserData, onResult: (Boolean) -> Unit) {
        try {
            preferencesUtil.saveUser(userData)
            Log.d("UserData", "saveDataToPreferences: ${preferencesUtil.getUser()}")
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

     fun checkUserInPrefs(onResult: (Boolean) -> Unit) {
        try {
            val user = preferencesUtil.getUser()
            if (user != null) {
                onResult(true)
            }
            onResult(false)
        } catch (e: Exception) {
            onResult(false)
        }
    }

}
