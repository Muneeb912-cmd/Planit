package com.example.eventmanagement.repository.firebase.envents_data

import com.example.eventmanagement.models.EventData

interface EventDataMethods {
    fun getAllEvents():List<EventData>
    fun getEventById(eventId:String):EventData?
    fun updateEventById(eventId: String,onResult: (Boolean)->Unit)
    fun deleteEventById(eventId: String,onResult: (Boolean)->Unit)
    fun getEventsByCreator(creatorId:String):List<EventData>
    fun observeAllEvents(onResult: (List<EventData>) -> Unit)
}