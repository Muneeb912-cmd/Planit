package com.example.eventmanagement.repository.firebase.login_signup

import com.example.eventmanagement.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthResult

interface LoginSignUpMethods {
    fun sendVerificationEmail()
    fun signOut(onResult: (Boolean) -> Unit)
    fun signInWithEmailPassword(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun signInWithGoogle(account: GoogleSignInAccount, onResult: (Boolean) -> Unit)
    fun checkEmailVerification(onResult: (Boolean) -> Unit)
    fun createUser(userData: User.UserData,onResult: (Boolean) -> Unit)
    fun signUpWithEmailPassword(email:String,password:String, onResult: (Result<String>) -> Unit)
}