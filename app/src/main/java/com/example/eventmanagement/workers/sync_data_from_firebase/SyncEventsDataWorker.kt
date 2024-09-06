package com.example.eventmanagement.workers.sync_data_from_firebase

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.repository.room_db.LocalDB
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncEventsDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    val firestore = FirebaseFirestore.getInstance()
    val database = LocalDB.getInstance(context)
    private val eventsDao = database.eventsDao()
    private val connectivityObserver = ConnectivityObserver(context)
    override fun doWork(): Result {
        return try {
            if (!connectivityObserver.isConnected) {
                return Result.retry()
            }
            runBlocking {
                val events = fetchEventsFromFirestore()
                syncEvents(events)
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private suspend fun fetchEventsFromFirestore(): List<EventData> = withContext(Dispatchers.IO) {
        try {
            val events = mutableListOf<EventData>()
            val querySnapshot =
                firestore.collection("Events").whereEqualTo("eventDeleted", false).get().await()
            for (document in querySnapshot.documents) {
                val event = document.toObject(EventData::class.java)
                event?.let { events.add(it) }
            }
            events
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun syncEvents(events: List<EventData>) {
        withContext(Dispatchers.IO) {
            val existingEvents = eventsDao.getAllEvents()

            // Determine events to update
            val eventsToUpdate = existingEvents.mapNotNull { existingEvent ->
                val newEvent = events.find { it.eventId == existingEvent.eventId }
                if (newEvent != null && !areEventsEqual(existingEvent, newEvent)) {
                    existingEvent.apply {
                        eventTitle = newEvent.eventTitle
                        eventOrganizer = newEvent.eventOrganizer
                        eventTiming = newEvent.eventTiming
                        eventCategory = newEvent.eventCategory
                        eventDescription = newEvent.eventDescription
                        eventLocation = newEvent.eventLocation
                        eventLong = newEvent.eventLong
                        eventLat = newEvent.eventLat
                        eventDate = newEvent.eventDate
                        isEventFeatured = newEvent.isEventFeatured
                        isEventPopular = newEvent.isEventPopular
                        numberOfPeopleAttending = newEvent.numberOfPeopleAttending
                        isEventPublic = newEvent.isEventPublic
                        eventStatus = newEvent.eventStatus
                        eventCreatedBy = newEvent.eventCreatedBy
                        isEventDeleted = newEvent.isEventDeleted
                    }
                } else {
                    null
                }
            }

            val eventsToInsert = events.filter { event ->
                existingEvents.none { it.eventId == event.eventId }
            }

            val eventIdsFromFirestore = events.map { it.eventId }.toSet()
            val eventsToDelete = existingEvents.filter { it.eventId !in eventIdsFromFirestore }

            for (event in eventsToUpdate) {
                eventsDao.updateEventById(
                    eventTitle = event.eventTitle,
                    eventOrganizer = event.eventOrganizer,
                    eventTiming = event.eventTiming,
                    eventCategory = event.eventCategory,
                    eventDescription = event.eventDescription,
                    eventLocation = event.eventLocation,
                    eventLong = event.eventLong,
                    eventLat = event.eventLat,
                    eventDate = event.eventDate,
                    isEventFeatured = event.isEventFeatured,
                    isEventPopular = event.isEventPopular,
                    numberOfPeopleAttending = event.numberOfPeopleAttending,
                    isEventPublic = event.isEventPublic,
                    eventStatus = event.eventStatus,
                    eventCreatedBy = event.eventCreatedBy,
                    isEventDeleted = event.isEventDeleted,
                    eventId = event.eventId.toString()
                )
            }

            for (event in eventsToInsert) {
                Log.d("EventsToAdd", "syncEvents: $event")
                eventsDao.saveEvent(event)
            }

            for (event in eventsToDelete) {
                Log.d("EventsToDelete", "syncEvents: $event")
                eventsDao.deleteEventById(event.eventId.toString())
            }
        }
    }


    private fun areEventsEqual(existingEvent: EventData, newEvent: EventData): Boolean {
        return existingEvent.eventTitle == newEvent.eventTitle &&
                existingEvent.eventOrganizer == newEvent.eventOrganizer &&
                existingEvent.eventTiming == newEvent.eventTiming &&
                existingEvent.eventCategory == newEvent.eventCategory &&
                existingEvent.eventDescription == newEvent.eventDescription &&
                existingEvent.eventLocation == newEvent.eventLocation &&
                existingEvent.eventLong == newEvent.eventLong &&
                existingEvent.eventLat == newEvent.eventLat &&
                existingEvent.eventDate == newEvent.eventDate &&
                existingEvent.isEventFeatured == newEvent.isEventFeatured &&
                existingEvent.isEventPopular == newEvent.isEventPopular &&
                existingEvent.numberOfPeopleAttending == newEvent.numberOfPeopleAttending &&
                existingEvent.isEventPublic == newEvent.isEventPublic &&
                existingEvent.eventStatus == newEvent.eventStatus &&
                existingEvent.eventCreatedBy == newEvent.eventCreatedBy &&
                existingEvent.isEventDeleted == newEvent.isEventDeleted
    }

}
