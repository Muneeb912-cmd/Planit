package com.example.eventmanagement.ui.bottomsheets.ediitprofile

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.models.UserUpdate
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
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
class EditProfileViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters
) : ViewModel() {

    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val editDataStates: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    fun updateUserData(
        userId: String,
        userName: String,
        userPhone: String,
        userDob: String,
        userImg: String?
    ) {
        _states.value = Response.Loading
        val updateData = UserUpdate(
            userId = userId,
            userName = userName,
            userPhone = userPhone,
            userDob = userDob,
            userImg = userImg
        )
        val jsonData = converters.fromUpdateUser(updateData)
        CoroutineScope(Dispatchers.IO).launch {
            val count = pendingOperationDao.countByDocumentId(userId, "UPDATE", "user")
            if (count > 0) {
                if (userImg != null) {
                    pendingOperationDao.updateByDocumentId(userId, "UPDATE", "user", jsonData)
                } else {
                    pendingOperationDao.updateByDocumentId(userId, "UPDATE", "user", jsonData)
                }
            } else {
                pendingOperationDao.insert(
                    PendingOperations(
                        operationType = OperationType.UPDATE,
                        documentId = userId,
                        data = jsonData,
                        userId = userId,
                        eventId = "",
                        dataType = "user"
                    )
                )
            }
        }
        _states.value = Response.Success(Unit)
    }

    fun updateUserBanStatus(userId: String, banStatus: Boolean, onResult: (Boolean) -> Unit) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = userId,
            data = banStatus.toString(),
            userId = userId,
            eventId = "",
            dataType = "user_suspension_status"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val count =
                pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_suspension_status")
            if (count > 0) {
                pendingOperationDao.updateByDocumentId(
                    userId,
                    "UPDATE",
                    "user_suspension_status",
                    banStatus.toString()
                )
            } else {
                pendingOperationDao.insert(pendingOperation)
            }
        }
        onResult(true)
    }

}