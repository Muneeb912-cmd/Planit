package com.example.eventmanagement.repository.firebase.events_data

import com.example.eventmanagement.models.EventData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class EventsDataMethodsImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventDataMethods {

    private var eventsListener: ListenerRegistration? = null

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

    override fun getEventById(eventId: String): EventData? {
        var event: EventData? = null
        firestore.collection("Events").document(eventId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    event = document.toObject(EventData::class.java)
                }
            }
            .addOnFailureListener {
                // Handle any errors
            }
        return event
    }

    override fun updateEventById(eventId: String, onResult: (Boolean) -> Unit) {
        firestore.collection("Events").document(eventId)
            .update("fieldName", "newValue")  // Update specific fields as needed
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    override fun deleteEventById(eventId: String, onResult: (Boolean) -> Unit) {
        firestore.collection("Events").document(eventId)
            .delete()
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


    fun removeEventsListener() {
        eventsListener?.remove()
    }
}
