package com.example.eventmanagement.ui.fragments.events

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.FavEvents
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
class EventsViewModel  @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters
) : ViewModel() {

    fun addEventToUserFav(
        userId: String,
        eventData: EventData,
        onResult: (Boolean, String) -> Unit
    ) {
        addRemoveEventFromUserFav(userId, eventData, "add")
        onResult(true, "All Good")
    }

    fun removeEventFromUserFav(
        userId: String,
        eventData: EventData,
        onResult: (Boolean, String) -> Unit
    ) {
        addRemoveEventFromUserFav(userId, eventData, "del")
        onResult(true, "All Good")
    }

    private fun addRemoveEventFromUserFav(userId: String, eventData: EventData, key: String) {
        val favEvents = FavEvents(
            userId = userId,
            eventId = eventData.eventId.toString()
        )
        val jsonData = converters.fromFavEvent(favEvents)

        CoroutineScope(Dispatchers.IO).launch {
            if (key == "add") {
                val deleteCount = pendingOperationDao.countByDocumentId(
                    eventData.eventId.toString(),
                    "DELETE",
                    "fav_event"
                )

                if (deleteCount > 0) {
                    pendingOperationDao.delete(userId, eventData.eventId.toString(), "fav_event")
                }

                val pendingOperation = PendingOperations(
                    operationType = OperationType.ADD,
                    documentId = eventData.eventId.toString(),
                    data = jsonData,
                    userId = userId,
                    eventId = eventData.eventId.toString(),
                    dataType = "fav_event"
                )
                pendingOperationDao.insert(pendingOperation)

            } else if (key == "del") {
                val event = converters.fromEvent(eventData)

                val addCount = pendingOperationDao.countByDocumentId(
                    eventData.eventId.toString(),
                    "ADD",
                    "fav_event"
                )

                if (addCount > 0) {
                    pendingOperationDao.delete(userId, eventData.eventId.toString(), "fav_event")
                }

                val pendingOperation = PendingOperations(
                    operationType = OperationType.DELETE,
                    documentId = userId,
                    data = event,
                    userId = userId,
                    eventId = eventData.eventId.toString(),
                    dataType = "fav_event"
                )
                pendingOperationDao.insert(pendingOperation)
            }
        }
    }
}
