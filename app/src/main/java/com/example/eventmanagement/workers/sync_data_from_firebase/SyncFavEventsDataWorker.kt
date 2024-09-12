package com.example.eventmanagement.workers.sync_data_from_firebase

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.eventmanagement.models.FavEvents
import com.example.eventmanagement.repository.room_db.LocalDB
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SyncFavEventsDataWorker (
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = LocalDB.getInstance(context)
    private val favEventsDao = database.favEventsDao()
    private val connectivityObserver = ConnectivityObserver(context)

    override fun doWork(): Result {
        return try {
            if (!connectivityObserver.isConnected) {
                return Result.retry()
            }
            runBlocking {
                val favEvents = fetchFavEventsFromFirestore()
                syncFavEvents(favEvents)
            }
            Result.success()
        } catch (exception: Exception) {
            Log.e("Worker", "Error in syncing fav events", exception)
            Result.failure()
        }
    }

    private suspend fun fetchFavEventsFromFirestore(): List<FavEvents> =
        withContext(Dispatchers.IO) {
            try {
                val favEvents = mutableListOf<FavEvents>()
                val querySnapshot = firestore.collection("UserData")
                    .document(auth.currentUser?.uid.orEmpty())
                    .collection("FavEvents")
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val event = document.toObject(FavEvents::class.java)
                    event?.let { favEvents.add(it) }
                }
                favEvents
            } catch (e: Exception) {
                Log.e("Worker", "Error fetching fav events", e)
                emptyList()
            }
        }

    private suspend fun syncFavEvents(favEvents: List<FavEvents>) {
        withContext(Dispatchers.IO) {
            val existingFavEvents = favEventsDao.getAllCurrentUserFavEvents(auth.currentUser?.uid.orEmpty())

            // Determine favorite events to update
            val favEventsToUpdate = existingFavEvents.mapNotNull { existingEvent ->
                val newFavEvent = favEvents.find { it.eventId == existingEvent.eventId }
                if (newFavEvent != null && !areFavEventsEqual(existingEvent, newFavEvent)) {
                    existingEvent.apply {
                        userId = auth.currentUser?.uid.toString()
                        eventId = newFavEvent.eventId
                    }
                } else {
                    null
                }
            }

            // Determine favorite events to insert
            val favEventsToInsert = favEvents.mapNotNull { newFavEvent ->
                val eventNotExists = existingFavEvents.none { it.eventId == newFavEvent.eventId }
                if (eventNotExists) {
                    newFavEvent.apply {
                        userId = auth.currentUser?.uid.toString()
                        eventId = newFavEvent.eventId
                    }
                } else {
                    null
                }
            }

            // Determine favorite events to delete
            val favEventIdsFromFirestore = favEvents.map { it.eventId }.toSet()
            val favEventsToDelete = existingFavEvents.filter { it.eventId !in favEventIdsFromFirestore }

            // Perform updates
            for (event in favEventsToUpdate) {
                Log.d("Worker", "Updating fav event: $event")
                favEventsDao.updateEventToUserFav(event)
            }

            // Perform inserts
            for (event in favEventsToInsert) {
                Log.d("Worker", "Inserting fav event: $event")
                favEventsDao.addEventToUserFav(event)
            }

            // Perform deletions
            for (event in favEventsToDelete) {
                Log.d("Worker", "Deleting fav event: $event")
                favEventsDao.removeEventFromUserFav(event.userId.toString(),event.eventId.toString())
            }
        }
    }


    private fun areFavEventsEqual(existingFavEvent: FavEvents, newFavEvent: FavEvents): Boolean {
        return existingFavEvent.userId == newFavEvent.userId &&
                existingFavEvent.eventId == newFavEvent.eventId
    }
}
