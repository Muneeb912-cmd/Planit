package com.example.eventmanagement.ui.activities.event_invites

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventInvites
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventInviteViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters,
) : ViewModel() {

    fun updateInviteStatus(
        invite: EventInvites,
        userId: String,
        newStatus: String,
        onResult: (Boolean, String) -> Unit
    ) {
        acceptRejectInvite(invite.inviteId.toString(), newStatus)
        addRemoveUserAsEventAttendee(invite.eventId.toString(), userId, "add")
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

    private fun addRemoveUserAsEventAttendee(eventId: String, userId: String, key: String) {
        val attendee = Attendees(
            userId = userId,
            eventId = eventId
        )
        val jsonData = converters.fromAttendee(attendee)

        CoroutineScope(Dispatchers.IO).launch {
            if (key == "add") {
                val deleteCount = pendingOperationDao.countByDocumentId(
                    eventId,
                    "DELETE",
                    "attendee"
                )
                if (deleteCount > 0) {
                    pendingOperationDao.delete(userId, eventId, "attendee")
                }

                // Add the new ADD operation
                val pendingOperation = PendingOperations(
                    operationType = OperationType.ADD,
                    documentId = eventId,
                    data = jsonData,
                    userId = userId,
                    eventId = eventId,
                    dataType = "attendee"
                )
                pendingOperationDao.insert(pendingOperation)
            }
        }
    }
}
