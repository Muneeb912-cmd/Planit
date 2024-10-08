package com.example.eventmanagement.ui.bottomsheets.eventdetails

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.Attendees
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
class EventDetailsViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters,

) : ViewModel() {

    fun addUserAsAttendee(eventId: String, userId: String, onResult: (Boolean) -> Unit) {
        addRemoveUserAsEventAttendee(eventId, userId, "add")
        onResult(true)
    }

    fun removeUserAsAttendee(eventId: String, userId: String, onResult: (Boolean) -> Unit) {
        addRemoveUserAsEventAttendee(eventId, userId, "del")
        rejectInvite(eventId, userId)
        onResult(true)
    }


    private fun rejectInvite(eventId: String, userId: String,) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = eventId,
            data = "reject",
            userId = userId,
            eventId = eventId,
            dataType = "reject_event_invite"
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

            } else if (key == "del") {
                // Check if there is an existing ADD operation for this event and user
                val addCount = pendingOperationDao.countByDocumentId(
                    eventId,
                    "ADD",
                    "attendee"
                )

                if (addCount > 0) {
                    // If there is, delete the existing ADD operation
                    pendingOperationDao.delete(userId, eventId, "attendee")
                }

                // Add the new DELETE operation
                val pendingOperation = PendingOperations(
                    operationType = OperationType.DELETE,
                    documentId = userId,
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