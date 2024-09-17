package com.example.eventmanagement.repository.roomDb

import androidx.room.TypeConverter
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.FavEvents
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.User
import com.example.eventmanagement.models.UserUpdate
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

class Converters {
    private val moshi =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User.UserData::class.java)
    private val eventAdapter = moshi.adapter(EventData::class.java)
    private val inviteAdapter = moshi.adapter(Invites::class.java)
    private val attendeeAdapter = moshi.adapter(Attendees::class.java)
    private val updateUserAdapter = moshi.adapter(UserUpdate::class.java)
    private val favEventAdapter = moshi.adapter(FavEvents ::class.java)


    @TypeConverter
    fun fromUser(user: User.UserData): String {
        return userAdapter.toJson(user)
    }

    @TypeConverter
    fun toUser(userJson: String): User.UserData {
        return userAdapter.fromJson(userJson) ?: throw IllegalArgumentException("Invalid User data")
    }

    @TypeConverter
    fun fromFavEvent(favEvent: FavEvents): String {
        return favEventAdapter.toJson(favEvent)
    }

    @TypeConverter
    fun toFavEvent(favEvent: String):FavEvents {
        return favEventAdapter.fromJson(favEvent) ?: throw IllegalArgumentException("Invalid User data")
    }

    @TypeConverter
    fun fromEvent(event: EventData): String {
        return eventAdapter.toJson(event)
    }

    @TypeConverter
    fun toEvent(eventJson: String): EventData {
        return eventAdapter.fromJson(eventJson)
            ?: throw IllegalArgumentException("Invalid Event data")
    }

    @TypeConverter
    fun fromInvite(invite: Invites): String {
        return inviteAdapter.toJson(invite)
    }

    @TypeConverter
    fun toInvite(inviteJson: String): Invites {
        return inviteAdapter.fromJson(inviteJson)
            ?: throw IllegalArgumentException("Invalid Invite data")
    }

    @TypeConverter
    fun fromAttendee(attendee: Attendees): String {
        return attendeeAdapter.toJson(attendee)
    }

    @TypeConverter
    fun toAttendee(attendeeJson: String): Attendees {
        return attendeeAdapter.fromJson(attendeeJson)
            ?: throw IllegalArgumentException("Invalid Attendee data")
    }

    @TypeConverter
    fun fromUpdateUser(userUpdate: UserUpdate): String {
        return updateUserAdapter.toJson(userUpdate)
    }

    @TypeConverter
    fun toUpdateUser(userUpdateJson: String): UserUpdate {
        return updateUserAdapter.fromJson(userUpdateJson)
            ?: throw IllegalArgumentException("Invalid Attendee data")
    }
}
