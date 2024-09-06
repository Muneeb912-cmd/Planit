package com.example.eventmanagement.models

import com.google.firebase.Timestamp

data class EventInvites (
    var inviteId:String?=null,
    var eventId:String?=null,
    var eventTitle: String?=null,
    var eventOrganizer: String?=null,
    var eventTiming: String?=null,
    var eventDate: String?=null,
    var senderName: String?=null,
    var inviteTime: String?=null,
    var inviteStatus: String?=null,
)