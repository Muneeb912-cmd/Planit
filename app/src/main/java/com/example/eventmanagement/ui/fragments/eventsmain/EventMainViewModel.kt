package com.example.eventmanagement.ui.fragments.eventsmain

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.utils.PreferencesUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventMainViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods,
    private val pendingOperationDao: PendingOperationDao,
    private val preferencesUtil: PreferencesUtil,
    private val userDataMethods: UserDataMethods
)  : ViewModel() {

    fun signOut(onResult: (Boolean, String)->Unit) {
        try {
            loginSignUpMethods.signOut { userSignedOut ->
                if (userSignedOut) {
                    preferencesUtil.deleteUser()
                    userDataMethods.removeCurrentUserListener()
                    onResult(true,"User Signed Out")
                }
            }
        } catch (e: Exception) {
            onResult(false,"Error Signing Out User")
        }
    }
    fun updateUserLocation(userId: String, newLocation: String, onResult: (Boolean) -> Unit) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = userId,
            data = newLocation,
            userId = userId,
            eventId = "",
            dataType = "user_location"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val count = pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_location")
            if (count > 0) {
                pendingOperationDao.updateByDocumentId(
                    userId,
                    "UPDATE",
                    "user_location",
                    newLocation
                )
            } else {
                pendingOperationDao.insert(pendingOperation)
            }
        }
        onResult(true)
    }
}