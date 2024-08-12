package com.example.eventmanagement.ui.fragments.signup

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.User
import com.example.eventmanagement.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val validators: Validators
) : ViewModel() {

    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    var isRoleSelected: Boolean = false
    var isDataComplete: Boolean = false

    private val _errors = MutableStateFlow<Map<String, String?>>(emptyMap())
    val errors: StateFlow<Map<String, String?>> get() = _errors


    fun updateUserInfo(key: String, value: String) {
        val currentUser = _user.value
        val updatedUser = currentUser.copy(
            fullName = if (key == "fullName") value else currentUser.fullName,
            email = if (key == "email") value else currentUser.email,
            phone = if (key == "phone") value else currentUser.phone,
            dob = if (key == "dob") value else currentUser.dob,
            password = if (key == "password") value else currentUser.password,
            role = if (key == "role") value else currentUser.role,
            img = if (key == "img") value else currentUser.img
        )
        validateField(key, value)
        checkIfDataComplete()
        _user.value = updatedUser
        println("Updated User: $updatedUser")
    }

    private fun validateField(field: String, value: String) {
        val updatedErrors = _errors.value.toMutableMap()

        when (field) {
            "fullName" -> updatedErrors["fullName"] =
                if (validators.validateName(value)) null else "Invalid name, Example Input: (Ali Ahmad) "

            "email" -> updatedErrors["email"] =
                if (validators.validateEmail(value)) null else "Invalid email, Example Input: (user@mail.com)"

            "phone" -> updatedErrors["phone"] =
                if (validators.validatePhone(value)) null else "Invalid phone, Example Input: (+920000000000)"

            "password" -> updatedErrors["password"] =
                if (validators.validatePassword(value)) null else "Invalid password. Password should contain at least one special character, one uppercase letter, one number, and be at least 6 characters long."

        }
        _errors.value = updatedErrors
    }
    private fun checkIfDataComplete() {
        isDataComplete = !user.value.fullName.isNullOrEmpty() &&
                !user.value.email.isNullOrEmpty() &&
                !user.value.phone.isNullOrEmpty() &&
                !user.value.dob.isNullOrEmpty() &&
                !user.value.password.isNullOrEmpty()&&
                !user.value.img.isNullOrEmpty()
    }
}

