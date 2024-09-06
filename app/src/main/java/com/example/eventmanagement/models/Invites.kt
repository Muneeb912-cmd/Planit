package com.example.eventmanagement.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "invites",
    foreignKeys = [
        ForeignKey(
            entity = EventData::class,
            parentColumns = ["eventId"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["inviteId","eventId"], unique = true)]
)
data class Invites(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var inviteId:String?=null,
    var eventId:String?=null,
    var senderId:String?=null,
    var receiverId:String?=null,
    var inviteStatus:String?=null,
    var inviteTime: String? =null
)
