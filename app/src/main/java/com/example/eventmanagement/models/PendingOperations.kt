package com.example.eventmanagement.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperations(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var operationType: OperationType,
    var documentId: String,
    var data: String,
    var timestamp: Long = System.currentTimeMillis(),
    var userId:String,
    var eventId:String,
    var dataType:String

)

enum class OperationType {
    ADD,
    UPDATE,
    DELETE
}
