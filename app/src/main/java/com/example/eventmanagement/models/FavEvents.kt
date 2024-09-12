package com.example.eventmanagement.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fav_events",
    foreignKeys = [
        ForeignKey(
            entity = User.UserData::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventData::class,
            parentColumns = ["eventId"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index(value = ["userId", "eventId"], unique = true)],
)
data class FavEvents(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var userId: String? = null,
    var eventId: String? = null
)