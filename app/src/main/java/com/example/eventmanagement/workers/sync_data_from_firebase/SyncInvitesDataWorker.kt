package com.example.eventmanagement.workers.sync_data_from_firebase

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eventmanagement.models.Invites
import com.example.eventmanagement.repository.room_db.LocalDB
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncInvitesDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    val firestore = FirebaseFirestore.getInstance()
    val database = LocalDB.getInstance(context)
    private val invitesDao = database.invitesDao()
    private val connectivityObserver = ConnectivityObserver(context)
    override fun doWork(): Result {
        return try {
            if (!connectivityObserver.isConnected) {
                return Result.retry()
            }
            runBlocking {
                val invites = fetchInvitesFromFirestore()
                Log.d("Worker", "invitesFromFirebase: $invites")
                syncInvites(invites)
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private suspend fun fetchInvitesFromFirestore(): List<Invites> = withContext(Dispatchers.IO) {
        try {
            val invites = mutableListOf<Invites>()
            val querySnapshot = firestore.collection("Invites").get().await()
            for (document in querySnapshot.documents) {
                val invite = document.toObject(Invites::class.java)
                invite?.let { invites.add(it) }
            }
            invites
        } catch (e: Exception) {
            Log.e("Worker", "Error fetching invites", e)
            emptyList()
        }
    }


    private suspend fun syncInvites(invites: List<Invites>) {
        withContext(Dispatchers.IO) {
            val existingInvites = invitesDao.getAllInvites()

            // Determine invites to update
            val invitesToUpdate = existingInvites.mapNotNull { existingInvite ->
                val newInvite = invites.find { it.inviteId == existingInvite.inviteId }
                if (newInvite != null && !areInvitesEqual(existingInvite, newInvite)) {
                    existingInvite.apply {
                        eventId = newInvite.eventId
                        senderId = newInvite.senderId
                        receiverId = newInvite.receiverId
                        inviteStatus = newInvite.inviteStatus
                        inviteTime = newInvite.inviteTime
                    }
                } else {
                    null
                }
            }

            // Determine invites to insert
            val invitesToInsert = invites.filter { invite ->
                existingInvites.none { it.inviteId == invite.inviteId }
            }

            // Determine invites to delete
            val inviteIdsFromFirestore = invites.map { it.inviteId }.toSet()
            val invitesToDelete = existingInvites.filter { it.inviteId !in inviteIdsFromFirestore }

            // Perform updates
            for (invite in invitesToUpdate) {
                Log.d("Worker", "Updating invite: $invite")
                invitesDao.updateInviteById(
                    eventId = invite.eventId,
                    senderId = invite.senderId,
                    receiverId = invite.receiverId,
                    inviteStatus = invite.inviteStatus,
                    inviteTime = invite.inviteTime,
                    inviteId = invite.inviteId.toString()
                )
            }

            // Perform inserts
            for (invite in invitesToInsert) {
                Log.d("Worker", "Inserting invite: $invite")
                invitesDao.createInvite(invite)
            }

            // Perform deletions
            for (invite in invitesToDelete) {
                Log.d("Worker", "Deleting invite: $invite")
                invitesDao.deleteInvite(invite.eventId.toString(),invite.receiverId.toString())
            }
        }
    }

    private fun areInvitesEqual(existingInvite: Invites, newInvite: Invites): Boolean {
        return existingInvite.eventId == newInvite.eventId &&
                existingInvite.senderId == newInvite.senderId &&
                existingInvite.receiverId == newInvite.receiverId &&
                existingInvite.inviteStatus == newInvite.inviteStatus &&
                existingInvite.inviteTime == newInvite.inviteTime
    }
}


