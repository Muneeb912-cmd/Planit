package com.example.eventmanagement.ui.fragments.adminmain

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminMainViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
) : ViewModel() {
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