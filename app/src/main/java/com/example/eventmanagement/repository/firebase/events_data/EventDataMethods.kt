package com.example.eventmanagement.repository.firebase.events_data

import com.example.eventmanagement.models.EventData

interface EventDataMethods {
    fun getAllEvents(): List<EventData>
    fun getEventById(eventId: String): EventData?
    fun updateEventById(eventId: String, onResult: (Boolean) -> Unit)
    fun deleteEventById(eventId: String, onResult: (Boolean) -> Unit)
    fun getEventsByCreator(creatorId: String): List<EventData>
    fun observeAllEvents(onResult: (List<EventData>) -> Unit)
    fun saveEvent(eventData: EventData, onResult: (Boolean, String) -> Unit)
    fun addEventToUserFav(
        userId: String,
        eventData: EventData,
        onResult: (Boolean, String) -> Unit
    )

    fun removeEventFromUserFav(
        userId: String,
        eventData: EventData,
        onResult: (Boolean, String) -> Unit
    )

    fun observeCurrentUserFavEvents(
        userId: String,
        onResult: (List<EventData>) -> Unit
    )

}