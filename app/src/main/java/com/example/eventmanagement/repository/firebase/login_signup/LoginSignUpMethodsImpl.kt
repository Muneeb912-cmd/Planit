package com.example.eventmanagement.repository.firebase.login_signup

import android.net.Uri
import android.util.Log
import com.example.eventmanagement.models.User
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class LoginSignUpMethodsImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : LoginSignUpMethods {
    override fun sendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EmailVerification", "Verification email sent")
                } else {
                    Log.e("EmailVerification", "Error: ${task.exception?.message}")
                }
            }
    }

    override fun signInWithGoogle(account: GoogleSignInAccount, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "Sign-in successful")
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        sendVerificationEmail()
                        onResult(false)
                    } else {
                        onResult(true)
                    }
                } else {
                    Log.e("GoogleSignIn", "Error: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }

    override fun signInWithEmailPassword(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        onResult(true, null)
                    } else {
                        onResult(false, "User does not exist or credentials are incorrect")
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }


    override fun signOut(onResult: (Boolean) -> Unit) {
        try {
            auth.signOut()
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }


    override fun checkEmailVerification(onResult: (Boolean) -> Unit) {
        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(auth.currentUser?.isEmailVerified == true)
            } else {
                onResult(false)
            }
        }
    }

    override fun createUser(userData: User.UserData, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userDataCopy = User.UserData(
                userId = userId,
                userName = userData.userName,
                userEmail = userData.userEmail,
                userPhone = userData.userPhone,
                userDob = userData.userDob,
                userRole = userData.userRole,
                userImg = userData.userImg,
                userLocation = userData.userLocation,
                userLoginType = userData.userLoginType,
                isNotificationsAllowed = userData.isNotificationsAllowed,
                isProfilePrivate = userData.isProfilePrivate
            )
            val userImageUri = userDataCopy.userImg
            if (!userImageUri.isNullOrEmpty()) {
                val storageRef = firebaseStorage.reference.child("ProfileImages/$userId")
                val uploadTask = storageRef.putFile(Uri.parse(userImageUri))

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val updatedUserData = userDataCopy.copy(userImg = downloadUrl.toString())
                        saveUserDataToFirestore(userId, updatedUserData, onResult)
                    }.addOnFailureListener { exception ->
                        Log.e("CreateUser", "Failed to get download URL", exception)
                        onResult(false)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("CreateUser", "Image upload failed", exception)
                    onResult(false)
                }
            } else {
                saveUserDataToFirestore(userId, userDataCopy, onResult)
            }
        } else {
            Log.e("CreateUser", "No authenticated user found")
            onResult(false)
        }
    }

    override fun signUpWithEmailPassword(
        email: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        onResult(Result.success(user.uid))
                    } else {
                        onResult(Result.failure(Exception("User is null")))
                    }
                } else {
                    Log.e("EmailSignUp", "Error: ${task.exception?.message}")
                    onResult(Result.failure(task.exception ?: Exception("Sign-up failed")))
                }
            }
    }

    override fun sendResetPasswordEmail(email: String, onResult: (Boolean) -> Unit) {
        return try {
            auth.sendPasswordResetEmail(email)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }

    private fun saveUserDataToFirestore(
        userId: String,
        userData: User.UserData,
        onResult: (Boolean) -> Unit
    ) {
        val userDocRef = firestore.collection("UserData").document(userId)
        userDocRef.set(userData)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                Log.e("CreateUser", "Firestore write failed", exception)
                onResult(false)
            }
    }

}
