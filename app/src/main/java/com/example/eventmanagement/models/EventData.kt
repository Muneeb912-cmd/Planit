package com.example.eventmanagement.models

data class EventData(
    val eventId:String?=null,
    val eventTitle: String?=null,
    val eventOrganizer: String?=null,
    val eventTiming: String?=null,
    val eventCategory: String?=null,
    val eventDescription: String?=null,
    val eventLocation: String?=null,
    val eventDate: String?=null,
    val isEventFeatured:Boolean?=null,
    val isEventPopular:Boolean?=null,
    val numberOfPeopleAttending:Int?=null,
    val isEventPublic:Boolean?=null,
    val eventStatus:String?=null
)

