package com.example.eventmanagement.ui.fragments.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import com.example.eventmanagement.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val validators: Validators,
    private val loginSignUpMethods: LoginSignUpMethods,
    private val userDataMethods: UserDataMethods,
    private val preferencesUtil: PreferencesUtil
) : ViewModel() {

    private val _user = MutableStateFlow(User.UserData())
    val user: StateFlow<User.UserData> = _user.asStateFlow()

    private val _clearFocusEvent = MutableStateFlow(false)
    val clearFocusEvent: StateFlow<Boolean> get() = _clearFocusEvent

    var isRoleSelected: Boolean = false
    var isDataComplete: Boolean = false
    var isEmailVerified: Boolean = false
    var isDataValid: Boolean = false
    var accountExist: Boolean = false
    var loginType: String = ""

    private val _signUpResults = MutableStateFlow<Response<Unit>>(Response.Loading)
    val signUpResult: StateFlow<Response<Unit>> get() = _signUpResults.asStateFlow()

    private val _emailVerificationStatus = MutableStateFlow<Response<Unit>>(Response.Loading)
    val emailVerificationStatus: StateFlow<Response<Unit>> get() = _emailVerificationStatus.asStateFlow()


    fun updateUserInfo(key: String, value: String) {
        val currentUser = _user.value
        val updatedUser = currentUser.copy(
            userName = if (key == "fullName") value else currentUser.userName,
            userEmail = if (key == "email") value else currentUser.userEmail,
            userPhone = if (key == "phone") value else currentUser.userPhone,
            userDob = if (key == "dob") value else currentUser.userDob,
            userPassword = if (key == "password") value else currentUser.userPassword,
            userRole = if (key == "role") value else currentUser.userRole,
            userImg = if (key == "img") value else currentUser.userImg,
            userLocation = if (key == "location") value else currentUser.userLocation,
            userLoginType = if (key == "loginType") value else currentUser.userLoginType,
            profilePrivate = if (key == "profile") value == "Yes" else currentUser.profilePrivate,
            notificationsAllowed = if (key == "notification") value == "Yes" else currentUser.notificationsAllowed,
            userId = if (key == "userId") value else currentUser.userId,
            userBanned = if (key == "userBanned") value == "Yes" else currentUser.userBanned
        )
        _user.value = updatedUser
        println("Updated User: $updatedUser")
    }


    fun checkIfDataComplete() {
        isDataComplete = !user.value.userName.isNullOrEmpty() &&
                !user.value.userEmail.isNullOrEmpty() &&
                !user.value.userPhone.isNullOrEmpty() &&
                !user.value.userDob.isNullOrEmpty() &&
                (loginType == "google" || !user.value.userPassword.isNullOrEmpty())
    }

    fun createUserAccount() {
        if (loginType == "google") {
            addUserDataToFirestore(null)
        } else {
            _signUpResults.value = Response.Loading
            viewModelScope.launch {
                try {
                    loginSignUpMethods.signUpWithEmailPassword(
                        user.value.userEmail.toString(),
                        user.value.userPassword.toString()
                    ) { result ->
                        if (result.isSuccess) {
                            val userId = result.getOrNull()
                            if (userId != null) {
                                checkVerificationEmail()
                                addUserDataToFirestore(userId)
                                _signUpResults.value = Response.Success(Unit)
                                accountExist = false

                            } else {
                                _signUpResults.value =
                                    Response.Error(Exception("Failed to retrieve user ID or User already exist"))
                                accountExist = true
                            }
                        } else {
                            _signUpResults.value = Response.Error(Exception("Sign-up failed"))
                            accountExist = true
                        }
                    }
                } catch (e: Exception) {
                    _signUpResults.value = Response.Error(e)
                }
            }
        }
    }


    fun checkVerificationEmail() {
        _emailVerificationStatus.value = Response.Loading
        viewModelScope.launch {
            try {
                loginSignUpMethods.checkEmailVerification { verified ->
                    if (verified) {
                        _emailVerificationStatus.value = Response.Success(Unit)
                        isEmailVerified = true
                    } else {
                        _emailVerificationStatus.value =
                            Response.Error(Exception("Email not verified"))
                        isEmailVerified = false
                    }
                }
            } catch (e: Exception) {
                _emailVerificationStatus.value = Response.Error(e)
                isEmailVerified = false
            }
        }
    }


    fun sendVerificationEmail() {
        viewModelScope.launch {
            _signUpResults.value = Response.Loading
            try {
                loginSignUpMethods.sendVerificationEmail()
                _signUpResults.value = Response.Success(Unit)
            } catch (e: Exception) {
                _signUpResults.value = Response.Error(e)
            }
        }
    }


    private fun addUserDataToFirestore(userId: String?) {
        updateUserInfo("location", "Lahore, PK")
        updateUserInfo("loginType", loginType)
        updateUserInfo("profile", "No")
        updateUserInfo("notification", "Yes")
        updateUserInfo("userBanned", "No")
        if (userId.isNullOrEmpty()) updateUserInfo("userId", userId.toString())
        if (isDataComplete) {
            loginSignUpMethods.createUser(user.value) { userCreated ->
                if (userCreated) {
                    _signUpResults.value = Response.Success(Unit)
                } else {
                    _signUpResults.value = Response.Error(Exception("User creation failed"))
                }
            }
        }
    }

    fun getCurrentUser(): User.UserData? {
        return userDataMethods.getCurrentUser()
    }


    fun saveDataToPreferences(onResult: (Boolean) -> Unit) {
        val user1 = getCurrentUser()
        updateUserInfo("userId", user1?.userId.toString())
        try {
            preferencesUtil.saveUser(user.value)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    fun clearUserData() {
        _user.value = User.UserData()
        isRoleSelected = false
        isDataComplete = false
        isDataValid = false
    }

    fun validateFullName(fullName: String): String? {
        return if (fullName.isEmpty()) {
            null
        } else if (validators.validateName(fullName)) {
            null
        } else {
            "Invalid Name, Example Input: (Ali Ahmad)"
        }
    }

    fun validateEmail(email: String): String? {
        return if (email.isEmpty()) {
            null
        } else if (validators.validateEmail(email)) {
            updateUserInfo("email", email)
            null
        } else {
            "Invalid Email, Example Input: (user@mail.com)"
        }
    }

    fun validatePassword(password: String): String? {
        return if (password.isEmpty()) {
            null
        } else if (validators.validatePassword(password)) {
            null
        } else {
            "Password must include a digit, special character, and a capital letter."
        }
    }

    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return if (password == confirmPassword) {
            null
        } else {
            "Confirm Password did not match with Password"
        }
    }

    fun checkIfImgSelected(): Boolean {
        return !user.value.userImg.isNullOrEmpty()
    }


    fun checkIfDataValid() {
        isDataValid = !validateFullName(_user.value.userName.toString()).isNullOrEmpty().not() &&
                !validateEmail(_user.value.userEmail.toString()).isNullOrEmpty().not() &&
                !validatePassword(_user.value.userPassword.toString()).isNullOrEmpty().not() &&
                !validateConfirmPassword(_user.value.userPassword.toString(), _user.value.userPassword.toString()).isNullOrEmpty().not()
    }


    fun triggerClearFocus() {
        _clearFocusEvent.value = true
    }

    fun unTriggerClearFocus() {
        _clearFocusEvent.value = false
    }
}
