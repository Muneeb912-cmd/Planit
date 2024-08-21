package com.example.eventmanagement.ui.fragments.forget_pass

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPassViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods
) : ViewModel() {
    fun sendPasswordRestEmail(email:String, onResult: (Boolean) -> Unit){
        loginSignUpMethods.sendResetPasswordEmail(email){emailSent->
            if(emailSent){
                onResult(true)
            }else{
                onResult(false)
            }
        }
    }
}