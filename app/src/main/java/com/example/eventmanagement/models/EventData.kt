package com.example.eventmanagement.models

data class EventData(
    var eventId:String?=null,
    var eventTitle: String?=null,
    var eventOrganizer: String?=null,
    var eventTiming: String?=null,
    var eventCategory: String?=null,
    var eventDescription: String?=null,
    var eventLocation: String?=null,
    var eventLong: String?=null,
    var eventLat: String?=null,
    var eventDate: String?=null,
    var isEventFeatured:Boolean?=null,
    var isEventPopular:Boolean?=null,
    var numberOfPeopleAttending:Int?=null,
    var isEventPublic:Boolean?=null,
    var eventStatus:String?=null,
    var eventCreatedBy:String?=null
)

