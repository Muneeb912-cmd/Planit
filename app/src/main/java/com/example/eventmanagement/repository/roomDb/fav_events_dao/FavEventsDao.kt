package com.example.eventmanagement.repository.roomDb.fav_events_dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventmanagement.models.FavEvents
import kotlinx.coroutines.flow.Flow

@Dao
interface FavEventsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEventToUserFav(favEvent: FavEvents)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateEventToUserFav(favEvent: FavEvents)

    @Query("DELETE FROM fav_events WHERE userId = :userId AND eventId = :eventId")
    suspend fun removeEventFromUserFav(userId: String, eventId: String)

    @Query("SELECT * FROM fav_events WHERE userId = :userId")
    fun getAllCurrentUserFavEvents(userId: String): List<FavEvents>

    @Query("SELECT eventId FROM fav_events WHERE userId = :userId")
    fun observeCurrentUserFavEvents(userId: String): Flow<List<String>>

}