package com.example.eventmanagement.repository.room_db.attendee_dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventmanagement.models.Attendees
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAttendee(attendee: Attendees)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAttendee(attendee: Attendees)
    @Query("SELECT * FROM attendees")
    suspend fun getAllAttendees():List<Attendees>

    @Query("DELETE FROM attendees WHERE userId = :userId AND eventId= :eventId")
    suspend fun removeAttendee(userId: String,eventId:String)

    @Query("SELECT COUNT(*) FROM attendees WHERE eventId = :eventId")
    suspend fun getPeopleGoingCount(eventId: String): Int

    @Query("SELECT userId FROM attendees WHERE eventId = :eventId")
    fun observeAttendeesByEventId(eventId: String): Flow<List<String>>

    @Query("SELECT * FROM attendees WHERE userId = :userId")
    fun observeCurrentUserFromAttendees(userId: String): Flow<List<Attendees>>
}