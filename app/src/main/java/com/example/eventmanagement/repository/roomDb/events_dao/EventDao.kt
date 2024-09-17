package com.example.eventmanagement.repository.roomDb.events_dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventmanagement.models.EventData
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE isEventDeleted = 0")
    fun getAllEvents(): List<EventData>

    @Query("SELECT * FROM events WHERE eventId = :eventId AND isEventDeleted = 0 LIMIT 1")
    suspend fun getEventById(eventId: String): EventData?

    @Query(
        """
        UPDATE events 
        SET 
            eventTitle = :eventTitle,
            eventOrganizer = :eventOrganizer,
            eventTiming = :eventTiming,
            eventCategory = :eventCategory,
            eventDescription = :eventDescription,
            eventLocation = :eventLocation,
            eventLong = :eventLong,
            eventLat = :eventLat,
            eventDate = :eventDate,
            isEventFeatured = :isEventFeatured,
            isEventPopular = :isEventPopular,
            numberOfPeopleAttending = :numberOfPeopleAttending,
            isEventPublic = :isEventPublic,
            eventStatus = :eventStatus,
            eventCreatedBy = :eventCreatedBy,
            isEventDeleted = :isEventDeleted
        WHERE eventId = :eventId
    """
    )
    suspend fun updateEventById(
        eventId: String,
        eventTitle: String?,
        eventOrganizer: String?,
        eventTiming: String?,
        eventCategory: String?,
        eventDescription: String?,
        eventLocation: String?,
        eventLong: String?,
        eventLat: String?,
        eventDate: String?,
        isEventFeatured: Boolean?,
        isEventPopular: Boolean?,
        numberOfPeopleAttending: Int?,
        isEventPublic: Boolean?,
        eventStatus: String?,
        eventCreatedBy: String?,
        isEventDeleted: Boolean?
    ): Int

    @Query("Delete from events WHERE eventId = :eventId")
    suspend fun deleteEventById(eventId: String)

    @Query("SELECT * FROM events WHERE eventCreatedBy = :creatorId AND isEventDeleted = 0")
    fun getEventsByCreator(creatorId: String): List<EventData>

    @Query("SELECT * FROM events WHERE isEventDeleted = 0")
    fun observeAllEvents(): Flow<List<EventData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEvent(eventData: EventData)

    @Query(
        """
        UPDATE events 
        SET
            eventStatus = :eventStatus
        WHERE eventId = :eventId
    """
    )
    suspend fun updateEventStatusId(
        eventId: String?,
        eventStatus: String?,
        ): Int
}