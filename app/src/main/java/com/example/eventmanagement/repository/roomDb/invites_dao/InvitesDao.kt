package com.example.eventmanagement.repository.roomDb.invites_dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.eventmanagement.models.Invites
import kotlinx.coroutines.flow.Flow

@Dao
interface InvitesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createInvite(invite: Invites)

    @Query("""
        UPDATE invites 
        SET 
            eventId = :eventId,
            senderId = :senderId,
            receiverId = :receiverId,
            inviteStatus = :inviteStatus,
            inviteTime = :inviteTime
        WHERE inviteId = :inviteId
    """)
    suspend fun updateInviteById(
        inviteId: String,
        eventId: String?,
        senderId: String?,
        receiverId: String?,
        inviteStatus: String?,
        inviteTime: String?
    ): Int
    @Query("SELECT * FROM invites")
    fun getAllInvites(): List<Invites>
    @Query("SELECT * FROM invites")
    fun observeAllInvites(): Flow<List<Invites>>

    @Query("SELECT * FROM invites WHERE receiverId = :userId")
    fun observeCurrentUserInvites(userId: String): Flow<List<Invites>>

    @Query("DELETE FROM invites WHERE eventId = :eventId AND receiverId = :userId")
    suspend fun deleteInvite(eventId: String, userId: String): Int

    @Query("UPDATE invites SET inviteStatus = :newStatus WHERE inviteId = :inviteId")
    suspend fun updateInviteStatus(inviteId: String, newStatus: String): Int
}