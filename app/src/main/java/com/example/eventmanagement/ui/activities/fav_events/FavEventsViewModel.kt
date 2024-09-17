package com.example.eventmanagement.ui.activities.fav_events

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.EventData
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
class FavEventsViewModel @Inject constructor(
    private val converters: Converters,
    private val pendingOperationDao: PendingOperationDao,
): ViewModel()  {
    fun removeEventFromUserFav(userId:String, eventData: EventData, onResult: (Boolean, String) -> Unit){
        val event=converters.fromEvent(eventData)
        val pendingOperation = PendingOperations(
            operationType = OperationType.DELETE,
            documentId = userId,
            data = event,
            userId = userId,
            eventId = eventData.eventId.toString(),
            dataType = "fav_event"
        )
        CoroutineScope(Dispatchers.IO).launch {
            val count = pendingOperationDao.countByDocumentId(
                eventData.eventId.toString(),
                "DELETE",
                "fav_event"
            )
            if (count > 0) {
                pendingOperationDao.delete(userId, eventData.eventId.toString(), "fav_event")
            } else {
                pendingOperationDao.insert(pendingOperation)
            }
        }
    }

}