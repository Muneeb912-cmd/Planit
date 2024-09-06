package com.example.eventmanagement.ui.activities.event_invites

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventInviteViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
) : ViewModel() {

    fun updateInviteStatus(
        inviteId: String,
        newStatus: String,
        onResult: (Boolean, String) -> Unit
    ) {
        acceptRejectInvite(inviteId, newStatus)
        onResult(true, "All Good!")
    }

    private fun acceptRejectInvite(inviteId: String, newStatus: String) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = inviteId,
            data = newStatus,
            userId = "",
            eventId = "",
            dataType = "event_invite"
        )
        CoroutineScope(Dispatchers.IO).launch {
            pendingOperationDao.insert(pendingOperation)

        }
    }
}
