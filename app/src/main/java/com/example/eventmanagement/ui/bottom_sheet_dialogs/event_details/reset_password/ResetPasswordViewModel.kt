package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.reset_password

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods
) :ViewModel(){
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