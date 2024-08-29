package com.example.eventmanagement.models

import com.google.firebase.Timestamp

data class Invites(
    var inviteId:String?=null,
    var eventId:String?=null,
    var senderId:String?=null,
    var receiverId:String?=null,
    var inviteStatus:String?=null,
    var inviteTime: Timestamp? =null
)
