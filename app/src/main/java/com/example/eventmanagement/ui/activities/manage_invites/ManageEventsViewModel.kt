package com.example.eventmanagement.ui.activities.manage_invites

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.models.User
import com.example.eventmanagement.repository.room_db.Converters
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ManageEventsViewModel @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters
) : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun createInvite(
        eventId: String,
        currentUserId: String,
        userData: User.UserData,
        onResult: (Boolean) -> Unit
    ) {
        val invite = Invites(
            eventId = eventId,
            inviteStatus = "pending",
            senderId = currentUserId,
            receiverId = userData.userId,
            inviteTime = getCurrentFormattedTimestamp()
        )
        addRemoveInviteFromPendingOperations(invite, "add") { result ->
            if (result)
                onResult(true)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateInvite(
        eventId: String,
        currentUserId: String,
        userData: User.UserData,
        onResult: (Boolean) -> Unit
    ) {
        val invite = Invites(
            eventId = eventId,
            inviteStatus = "pending",
            senderId = currentUserId,
            receiverId = userData.userId,
            inviteTime = getCurrentFormattedTimestamp()
        )
        updateInvitePendingOperation(invite) { result ->
            if (result)
                onResult(true)
        }
    }

    private fun getCurrentFormattedTimestamp(): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a z", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        return dateFormat.format(Date())
    }

    fun deleteInvite(
        eventId: String,
        currentUserId: String,
        userData: User.UserData,
        onResult: (Boolean) -> Unit
    ) {
        val invite = Invites(
            eventId = eventId,
            inviteStatus = "pending",
            senderId = currentUserId,
            receiverId = userData.userId,
            inviteTime = getCurrentFormattedTimestamp()
        )
        addRemoveInviteFromPendingOperations(invite, "del") { result ->
            if (result)
                onResult(true)
        }
    }

    private fun addRemoveInviteFromPendingOperations(
        inviteData: Invites,
        operation: String,
        onResult: (Boolean) -> Unit
    ) {
        val jsonData = converters.fromInvite(inviteData) ?: run {
            // Handle the null case or log an error
            onResult(false)
            return
        }

        val pendingOperation = PendingOperations(
            operationType = if (operation == "add") OperationType.ADD else OperationType.DELETE,
            documentId = inviteData.inviteId.toString(),
            data = jsonData,
            userId = inviteData.receiverId.toString(),
            eventId = inviteData.eventId.toString(),
            dataType = "invite"
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (operation == "add" || operation == "del") {
                pendingOperationDao.insert(pendingOperation)
                onResult(true) // Notify success
            } else {
                onResult(false) // Invalid operation type
            }
        }
    }

    private fun updateInvitePendingOperation(
        inviteData: Invites,
        onResult: (Boolean) -> Unit
    ) {
        val pendingOperation = PendingOperations(
            operationType = OperationType.UPDATE,
            documentId = inviteData.inviteId.toString(),
            data = "pending",
            userId = inviteData.receiverId.toString(),
            eventId = inviteData.eventId.toString(),
            dataType = "resend_invite"
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                pendingOperationDao.insert(pendingOperation)
                onResult(true) // Notify success
            }catch (e:Exception){
                onResult(false)
            }
        }
    }


}