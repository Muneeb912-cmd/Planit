package com.example.eventmanagement.workers.sync_data_from_firebase

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.repository.room_db.LocalDB
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncAttendeeDataWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    val firestore = FirebaseFirestore.getInstance()
    val database = LocalDB.getInstance(context)
    private val attendeeDao = database.attendeeDao()
    private val connectivityObserver = ConnectivityObserver(context)
    override fun doWork(): Result {
        return try {
            if (!connectivityObserver.isConnected) {
                return Result.retry()
            }
            runBlocking {
                val attendees = fetchAttendeesFromFirestore()
                syncAttendee(attendees)
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private suspend fun fetchAttendeesFromFirestore(): List<Attendees> =
        withContext(Dispatchers.IO) {
            try {
                val attendees = mutableListOf<Attendees>()
                val querySnapshot = firestore.collection("Attendees").get().await()
                for (document in querySnapshot.documents) {
                    val attendee = document.toObject(Attendees::class.java)
                    attendee?.let { attendees.add(it) }
                }
                attendees
            } catch (e: Exception) {
                Log.e("Worker", "Error fetching attendees", e)
                emptyList()
            }
        }

    private suspend fun syncAttendee(attendees: List<Attendees>) {
        withContext(Dispatchers.IO) {
            val existingAttendees = attendeeDao.getAllAttendees()

            // Determine attendees to update
            val attendeesToUpdate = existingAttendees.mapNotNull { existingAttendee ->
                val newAttendee = attendees.find { it.attendeeId == existingAttendee.attendeeId }
                if (newAttendee != null && !areAttendeesEqual(existingAttendee, newAttendee)) {
                    existingAttendee.apply {
                        userId = newAttendee.userId
                        eventId = newAttendee.eventId
                    }
                } else {
                    null
                }
            }

            val attendeesToInsert = attendees.filter { attendee ->
                existingAttendees.none { it.attendeeId == attendee.attendeeId }
            }

            val attendeeIdsFromFirestore = attendees.map { it.attendeeId }.toSet()
            val attendeesToDelete =
                existingAttendees.filter { it.attendeeId !in attendeeIdsFromFirestore }
            for (attendee in attendeesToUpdate) {
                Log.d("SyncAttendeeDataWorker", "Updating attendee: $attendee")
                attendeeDao.updateAttendee(attendee)
            }
            for (attendee in attendeesToInsert) {
                Log.d("SyncAttendeeDataWorker", "Inserting attendee: $attendee")
                attendeeDao.addAttendee(attendee)
            }
            for (attendee in attendeesToDelete) {
                Log.d("SyncAttendeeDataWorker", "Deleting attendee: $attendee")
                attendeeDao.removeAttendee(attendee.userId.toString(),attendee.eventId.toString())
            }
        }
    }

    private fun areAttendeesEqual(existingAttendee: Attendees, newAttendee: Attendees): Boolean {
        return existingAttendee.attendeeId == newAttendee.attendeeId &&
                existingAttendee.userId == newAttendee.userId &&
                existingAttendee.eventId == newAttendee.eventId
    }
}
