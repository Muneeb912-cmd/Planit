package com.example.eventmanagement.ui.fragments.profile

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.firebase.login_signup.LoginSignUpMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loginSignUpMethods: LoginSignUpMethods,
    private val preferencesUtil: PreferencesUtil,
    private val userDataMethods: UserDataMethods,
    private val pendingOperationDao: PendingOperationDao,
) : ViewModel() {

    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val profileStates: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    fun signOut() {
        _states.value = Response.Loading
        try {
            loginSignUpMethods.signOut { userSignedOut ->
                if (userSignedOut) {
                    _states.value = Response.Success(Unit)
                    preferencesUtil.deleteUser()
                    userDataMethods.removeCurrentUserListener()
                } else {
                    _states.value = Response.Error(Exception("Failed to sign out"))
                }
            }
        } catch (e: Exception) {
            _states.value = Response.Error(e)
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

    fun updateUserProfileStatus(userId: String, newSetting: Boolean, onResult: (Boolean) -> Unit) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = userId,
            data = newSetting.toString(),
            userId = userId,
            eventId = "",
            dataType = "user_profile_status"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val count =
                pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_profile_status")
            if (count > 0) {
                pendingOperationDao.updateByDocumentId(
                    userId,
                    "UPDATE",
                    "user_profile_status",
                    newSetting.toString()
                )
            } else {
                pendingOperationDao.insert(pendingOperation)
            }
        }
        onResult(true)
    }

    fun updateUserNotificationStatus(
        userId: String,
        newSetting: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = userId,
            data = newSetting.toString(),
            userId = userId,
            eventId = "",
            dataType = "user_notification_status"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val count =
                pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_notification_status")
            if (count > 0) {
                pendingOperationDao.updateByDocumentId(
                    userId,
                    "UPDATE",
                    "user_notification_status",
                    newSetting.toString()
                )
            } else {
                pendingOperationDao.insert(pendingOperation)
            }
        }
        onResult(true)
    }
}