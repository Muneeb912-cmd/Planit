package com.example.eventmanagement.ui.fragments.manage_events

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.room_db.Converters
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class EventManagementViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters
): ViewModel() {
    fun deleteEventById(eventData: EventData, onResult: (Boolean) -> Unit){
        saveEventAsPendingOperation(eventData)
        onResult(true)
    }

    private fun saveEventAsPendingOperation(eventData: EventData?) {
        if (eventData != null) {
            val jsonData = converters.fromEvent(eventData)
            val pendingOperation = PendingOperations(
                operationType = OperationType.DELETE,
                documentId = eventData.eventId.toString(),
                data = jsonData,
                userId = eventData.eventCreatedBy.toString(),
                eventId = eventData.eventId.toString(),
                dataType = "event"
            )
            CoroutineScope(Dispatchers.IO).launch {
                pendingOperationDao.insert(pendingOperation)
            }
        }
    }
}