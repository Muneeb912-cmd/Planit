package com.example.eventmanagement.repository.firebase.events_data

import android.util.Log
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class EventsDataMethodsImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventDataMethods {

    private var eventsListener: ListenerRegistration? = null
    private var favEventsListener: ListenerRegistration? = null
    private var eventAttendeesListener: ListenerRegistration? = null

    override fun getAllEvents(): List<EventData> {
        val eventsList = mutableListOf<EventData>()
        firestore.collection("Events")
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val events = snapshots.toObjects(EventData::class.java)
                    eventsList.addAll(events)
                }
            }
            .addOnFailureListener {
                // Handle any errors
            }
        return eventsList
    }

    override fun getEventById(eventId: String, onResult: (EventData?) -> Unit) {
        firestore.collection("Events")
            .whereEqualTo("eventDeleted", false)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val event = querySnapshot.documents[0].toObject(EventData::class.java)
                    onResult(event)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


    override fun updateEventById(eventId: String, eventData: EventData, onResult: (Boolean) -> Unit) {
        val eventMap = mapOf(
            "eventId" to eventData.eventId,
            "eventTitle" to eventData.eventTitle,
            "eventOrganizer" to eventData.eventOrganizer,
            "eventTiming" to eventData.eventTiming,
            "eventCategory" to eventData.eventCategory,
            "eventDescription" to eventData.eventDescription,
            "eventLocation" to eventData.eventLocation,
            "eventDate" to eventData.eventDate,
            "isEventFeatured" to eventData.isEventFeatured,
            "isEventPopular" to eventData.isEventPopular,
            "numberOfPeopleAttending" to eventData.numberOfPeopleAttending,
            "isEventPublic" to eventData.isEventPublic,
            "eventStatus" to eventData.eventStatus,
            "eventCreatedBy" to eventData.eventCreatedBy,
            "eventLong" to eventData.eventLong,
            "eventLat" to eventData.eventLat,
            "isEventDeleted" to eventData.isEventDeleted
        )

        firestore.collection("Events").document(eventId)
            .update(eventMap)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun deleteEventById(eventId: String, deleted: Boolean, onResult: (Boolean) -> Unit) {
        firestore.collection("Events").document(eventId)
            .update("eventDeleted", deleted)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun getEventsByCreator(creatorId: String): List<EventData> {
        val eventsList = mutableListOf<EventData>()
        firestore.collection("Events")
            .whereEqualTo("creatorId", creatorId)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots != null && !snapshots.isEmpty) {
                    val events = snapshots.toObjects(EventData::class.java)
                    eventsList.addAll(events)
                }
            }
            .addOnFailureListener {

            }
        return eventsList
    }

    override fun observeAllEvents(onResult: (List<EventData>) -> Unit) {
        eventsListener = firestore.collection("Events")
            .whereEqualTo("eventDeleted", false)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val events = snapshots.toObjects(EventData::class.java)
                    onResult(events)
                } else {
                    onResult(emptyList())
                }
            }
    }

    override fun saveEvent(eventData: EventData, onResult: (Boolean, String) -> Unit) {
        val eventsCollection = firestore.collection("Events")
        val newDocumentRef = eventsCollection.document()
        val updatedEventData = eventData.copy(eventId = newDocumentRef.id)

        newDocumentRef
            .set(updatedEventData)
            .addOnSuccessListener {
                onResult(true, "Event saved successfully with ID: ${newDocumentRef.id}.")
            }
            .addOnFailureListener { exception ->
                onResult(false, "Error saving event: ${exception.message}")
            }
    }

    override fun addEventToUserFav(
        userId: String,
        eventId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val userFavEventsRef = firestore.collection("UserData")
            .document(userId)
            .collection("FavEvents")
            .document(eventId)

        val data = mapOf("eventId" to eventId)

        userFavEventsRef.set(data).addOnSuccessListener {
            onResult(true, "")
        }.addOnFailureListener { exception ->
            onResult(false, exception.toString())
        }
    }


    override fun removeEventFromUserFav(
        userId: String,
        eventId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val userFavEventsRef = firestore.collection("UserData")
            .document(userId)
            .collection("FavEvents")
            .document(eventId)

        userFavEventsRef.delete().addOnSuccessListener {
            onResult(true, "Event removed from favorites successfully.")
        }.addOnFailureListener { exception ->
            onResult(false, "Failed to remove event from favorites: ${exception.message}")
        }
    }


    override fun observeCurrentUserFavEvents(
        userId: String,
        onResult: (List<String>) -> Unit
    ) {
        val userFavEventsRef = firestore.collection("UserData")
            .document(userId)
            .collection("FavEvents")

        userFavEventsRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                onResult(emptyList())
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val favEvents = snapshots.documents.mapNotNull { document ->
                    document.getString("eventId")
                }
                onResult(favEvents)
            } else {
                onResult(emptyList())
            }
        }
    }


    override fun addAttendeeUpdatePeopleGoingCount(
        eventId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        val eventRef = firestore.collection("Events").document(eventId)
        val attendeesCollection = firestore.collection("Attendees")

        firestore.runTransaction { transaction ->
            val eventSnapshot = transaction.get(eventRef)
            val newDocumentRef = attendeesCollection.document()
            val docId = newDocumentRef.id
            val attendeeSnapshot = transaction.get(attendeesCollection.document(docId))

            if (!attendeeSnapshot.exists()) {

                val newAttendee = mapOf(
                    "attendeeId" to docId,
                    "userId" to userId,
                    "eventId" to eventId
                )
                transaction.set(newDocumentRef, newAttendee)
            } else {
                throw Exception("User is already an attendee.")
            }

            val currentPeopleAttending = eventSnapshot.getLong("numberOfPeopleAttending") ?: 0
            val updatedPeopleAttending = currentPeopleAttending + 1
            transaction.update(eventRef, "numberOfPeopleAttending", updatedPeopleAttending)
            null
        }.addOnSuccessListener {
            onResult(true)
        }.addOnFailureListener {
            onResult(false)
        }
    }



    override fun removeAttendeeUpdatePeopleGoingCount(
        eventId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        val eventRef = firestore.collection("Events").document(eventId)

        firestore.collection("Attendees")
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val attendeeDoc = querySnapshot.documents[0].reference

                    firestore.runTransaction { transaction ->
                        val eventSnapshot = transaction.get(eventRef)
                        val attendeeSnapshot = transaction.get(attendeeDoc)

                        if (attendeeSnapshot.exists()) {
                            transaction.delete(attendeeDoc)
                        } else {
                            throw Exception("User is not an attendee.")
                        }

                        val currentPeopleAttending = eventSnapshot.getLong("numberOfPeopleAttending") ?: 0
                        val updatedPeopleAttending = currentPeopleAttending - 1

                        transaction.update(eventRef, "numberOfPeopleAttending", updatedPeopleAttending)
                    }.addOnSuccessListener {
                        onResult(true)
                    }.addOnFailureListener { exception ->
                        Log.e("FirestoreTransaction", "Transaction failed: ${exception.message}")
                        onResult(false)
                    }
                } else {
                    // No attendee found with the matching userId and eventId
                    onResult(false)
                }
            }.addOnFailureListener { exception ->
                Log.e("FirestoreQuery", "Query failed: ${exception.message}")
                onResult(false)
            }
    }


    // Method to observe attendees by eventId
    override fun observeAttendeesByEventId(eventId: String, onResult: (Boolean, List<String>) -> Unit) {
        val attendeesRef = firestore.collection("Attendees")
            .whereEqualTo("eventId", eventId)

        val listenerRegistration = attendeesRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                onResult(false, emptyList())
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val attendees = snapshots.documents.mapNotNull { document ->
                    document.getString("userId")
                }
                onResult(true, attendees)
            } else {
                onResult(true, emptyList())
            }
        }
        eventAttendeesListener = listenerRegistration
    }


    override fun observeCurrentUserFromAttendees(
        userId: String,
        onResult: (List<Attendees>) -> Unit
    ) {
        val userAttendeesRef = firestore.collection("Attendees")
            .whereEqualTo("userId", userId)

        userAttendeesRef.addSnapshotListener { snapshots, e ->
            if (e != null) {
                onResult(emptyList())
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val attendeesList = snapshots.documents.mapNotNull { document ->
                    document.toObject(Attendees::class.java)
                }
                onResult(attendeesList)
            } else {
                onResult(emptyList())
            }
        }
    }




    fun removeEventAttendeeListener() {
        eventAttendeesListener?.remove()
    }


    fun removeFavEventsListener() {
        favEventsListener?.remove()
    }

    fun removeEventsListener() {
        eventsListener?.remove()
    }
}
