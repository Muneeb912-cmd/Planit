package com.example.eventmanagement.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendees"
)
data class Attendees(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var attendeeId:String?=null,
    var userId: String? = null,
    var eventId: String? = null,
)
