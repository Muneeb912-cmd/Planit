package com.example.eventmanagement.repository.firebase.events_data

import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventData

interface EventDataMethods {
    fun getAllEvents(): List<EventData>
    fun getEventById(eventId: String, onResult: (EventData?) -> Unit)
    fun updateEventById(eventId: String,eventData:EventData,onResult: (Boolean) -> Unit)
    fun deleteEventById(eventId: String, deleted:Boolean, onResult: (Boolean) -> Unit)
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
        onResult: (List<String>) -> Unit
    )

    fun addAttendeeUpdatePeopleGoingCount(
        eventId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    )

    fun removeAttendeeUpdatePeopleGoingCount(
        eventId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    )

    fun observeAttendeesByEventId(eventId:String,onResult: (Boolean,List<String>) -> Unit)

    fun observeCurrentUserFromAttendees(userId: String, onResult: (List<Attendees>) -> Unit)

}