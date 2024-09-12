package com.example.eventmanagement.repository.room_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eventmanagement.models.PendingOperations
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pendingOperation: PendingOperations)

    @Query("SELECT * FROM pending_operations")
    fun getAll(): List<PendingOperations>

    @Query("SELECT * FROM pending_operations")
    fun observePendingOperations(): Flow<List<PendingOperations>>

    @Query("DELETE FROM pending_operations WHERE userId = :userId AND eventId = :eventId AND dataType = :dataType")
    fun delete(userId: String, eventId: String, dataType: String)

    @Query("SELECT COUNT(*) FROM pending_operations WHERE documentId = :documentId AND dataType= :dataType AND operationType= :operationType ")
    suspend fun countByDocumentId(documentId: String, operationType: String, dataType: String): Int


    @Query("SELECT COUNT(*) FROM pending_operations WHERE eventId = :eventId AND userId= :userId AND dataType= :dataType AND operationType= :operationType ")
    suspend fun countByEventIdUserId(eventId: String, userId:String,operationType: String, dataType: String): Int


    @Query(
        """
        UPDATE pending_operations 
        SET data = :data
        WHERE documentId = :documentId AND operationType=:operationType AND dataType=:dataType
    """
    )
    suspend fun updateByDocumentId(
        documentId: String,
        operationType: String,
        dataType: String,
        data: String
    )

    @Query("SELECT COUNT(*) FROM pending_operations WHERE documentId = :documentId AND dataType= :dataType AND operationType= :operationType ")
    suspend fun countByDocumentIdAndEvent(documentId: String, operationType: String, dataType: String): Int


    @Delete
    suspend fun deleteOperation(operation: PendingOperations)
}