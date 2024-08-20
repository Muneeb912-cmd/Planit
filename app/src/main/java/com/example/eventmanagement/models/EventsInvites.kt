package com.example.eventmanagement.models

data class EventsInvites(
    var eventId:String?=null,
    var senderId:String?=null,
    var receiverId:String?=null,
    var inviteStatus:String?=null,
    var inviteDateTime:String?=null
)
