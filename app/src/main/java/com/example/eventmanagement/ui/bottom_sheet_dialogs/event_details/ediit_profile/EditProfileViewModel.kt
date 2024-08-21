package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userDataMethods: UserDataMethods,
    private val loginSignUpMethods: LoginSignUpMethods
): ViewModel() {

    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val editDataStates: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    fun updateUserData(userId: String,userName:String,userEmail:String,userPhone:String,userDob:String,userImg:String){
        _states.value=Response.Loading
        viewModelScope.launch {
            userDataMethods.updateUserProfile(
                userId,userName,userEmail,userPhone,userDob,userImg
            ){result->
                if(result){
                    _states.value=Response.Success(Unit)
                }else{
                    _states.value=Response.Error(Exception("Error Updating User Data!"))
                }
            }
        }
    }

}