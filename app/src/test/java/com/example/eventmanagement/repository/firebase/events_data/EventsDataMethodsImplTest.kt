package com.example.eventmanagement.repository.firebase.events_data

import com.example.eventmanagement.models.EventData
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class EventsDataMethodsImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var collectionReference: CollectionReference
    private lateinit var eventsDataMethods: EventsDataMethodsImpl
    private lateinit var queryReference:CollectionReference
    @BeforeEach
    fun setUp() {
        firestore = mock()
        collectionReference = mock()
        queryReference = mock()
        whenever(firestore.collection("Events")).thenReturn(collectionReference)

        eventsDataMethods = EventsDataMethodsImpl(firestore)
    }


    @Test
    fun getAllEvents() = runBlocking {
        // Arrange
        val mockQuerySnapshot = mock<QuerySnapshot>()
        val eventData = EventData(eventId = "1", eventTitle = "Event 1")
        val eventList = listOf(eventData)

        // Ensure that the CollectionReference mock is returned correctly
        whenever(firestore.collection("Events")).thenReturn(collectionReference)

        // Mock the get method on the CollectionReference to return a Task<QuerySnapshot>
        val mockTask = mock<Task<QuerySnapshot>>()
        whenever(collectionReference.get()).thenReturn(mockTask)

        // Simulate a successful result with the addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
            successListener.onSuccess(mockQuerySnapshot)
            mockTask
        }

        whenever(mockQuerySnapshot.toObjects(EventData::class.java)).thenReturn(eventList)

        // Act
        val result = eventsDataMethods.getAllEvents()

        // Assert
        assertEquals(eventList, result)
    }


    @Test
    fun updateEventById() = runBlocking {
        // Arrange
        val eventId = "event_123"
        val eventData = EventData(eventId = eventId, eventTitle = "Updated Event")

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

        val mockDocumentReference = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()

        // Mock the document reference to return the mock DocumentReference
        whenever(firestore.collection("Events").document(eventId)).thenReturn(mockDocumentReference)

        // Mock the update method on the DocumentReference to return the mock Task
        whenever(mockDocumentReference.update(eventMap)).thenReturn(mockTask)

        // Simulate a successful result with the addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var updateResult = false
        eventsDataMethods.updateEventById(eventId, eventData) { success ->
            updateResult = success
        }

        // Assert
        assertEquals(true, updateResult)
    }


    @Test
    fun updateEventStatusById()= runBlocking {
        // Arrange
        val eventId = "event_123"
        val eventStatus = "Active"

        val eventMap = mapOf("eventStatus" to eventStatus)
        val mockDocumentReference = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()

        whenever(firestore.collection("Events")).thenReturn(collectionReference)

        whenever(collectionReference.document(eventId)).thenReturn(mockDocumentReference)

        whenever(mockDocumentReference.update(eventMap)).thenReturn(mockTask)

        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var updateResult = false
        eventsDataMethods.updateEventStatusById(eventId, eventStatus) { success ->
            updateResult = success
        }

        // Assert
        assertEquals(true, updateResult)
    }

    @Test
    fun deleteEventById() = runBlocking {
        // Arrange
        val eventId = "event_123"
        val deleted = true

        val mockDocumentReference = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()

        // Mock the Firestore collection to return the mock CollectionReference
        whenever(firestore.collection("Events")).thenReturn(collectionReference)

        // Ensure the CollectionReference returns the mock DocumentReference
        whenever(collectionReference.document(eventId)).thenReturn(mockDocumentReference)

        // Mock the update method on the DocumentReference to return the mock Task
        whenever(mockDocumentReference.update("eventDeleted", deleted)).thenReturn(mockTask)

        // Simulate a successful result with the addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var deleteResult = false
        eventsDataMethods.deleteEventById(eventId, deleted) { success ->
            deleteResult = success
        }

        // Assert
        assertEquals(true, deleteResult)
    }

    @Test
    fun getEventsByCreator()  = runBlocking {
        // Arrange
        val creatorId = "creator_123"
        val eventData = EventData(eventId = "1", eventTitle = "Event 1")
        val eventList = listOf(eventData)
        val mockQuerySnapshot = mock<QuerySnapshot>()
        val mockTask = mock<Task<QuerySnapshot>>()

        // Mock the Firestore collection and QuerySnapshot
        whenever(firestore.collection("Events")).thenReturn(collectionReference)
        whenever(collectionReference.whereEqualTo("creatorId", creatorId)).thenReturn(queryReference)
        whenever(queryReference.get()).thenReturn(mockTask)

        // Simulate a successful result with the addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<QuerySnapshot>
            successListener.onSuccess(mockQuerySnapshot)
            mockTask
        }
        whenever(mockQuerySnapshot.toObjects(EventData::class.java)).thenReturn(eventList)

        // Act
        val result = mutableListOf<EventData>()
        // Use a coroutine to handle asynchronous operation
        val job = launch {
            result.addAll(eventsDataMethods.getEventsByCreator(creatorId))
        }
        job.join() // Wait for the coroutine to complete

        // Assert
        assertEquals(eventList, result)
    }


    @Test
    fun saveEvent() = runBlocking {
        // Arrange
        val eventData = EventData(eventId = "event_123", eventTitle = "New Event")
        val newDocumentId = "new_event_id"
        val updatedEventData = eventData.copy(eventId = newDocumentId)
        val mockDocumentReference = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()

        // Mock the Firestore collection and document reference
        whenever(firestore.collection("Events")).thenReturn(collectionReference)
        whenever(collectionReference.document()).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.id).thenReturn(newDocumentId)
        whenever(mockDocumentReference.set(updatedEventData)).thenReturn(mockTask)

        // Simulate a successful result with addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var resultSuccess = false
        var resultMessage: String? = null
        eventsDataMethods.saveEvent(eventData) { success, message ->
            resultSuccess = success
            resultMessage = message
        }

        // Assert
        assertTrue(resultSuccess)
        assertEquals("Event saved successfully with ID: $newDocumentId.", resultMessage)
    }

    @Test
    fun addEventToUserFav() = runBlocking {
        // Arrange
        val userId = "user_123"
        val eventId = "event_456"
        val userFavEventsRef = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()

        val userDocumentReference:DocumentReference=mock()
        val favEventsCollectionReference:CollectionReference=mock()

        // Mock the Firestore collection and document reference
        whenever(firestore.collection("UserData")).thenReturn(collectionReference)
        whenever(collectionReference.document(userId)).thenReturn(userDocumentReference)
        whenever(userDocumentReference.collection("FavEvents")).thenReturn(favEventsCollectionReference)
        whenever(favEventsCollectionReference.document(eventId)).thenReturn(userFavEventsRef)
        whenever(userFavEventsRef.set(mapOf("eventId" to eventId))).thenReturn(mockTask)

        // Simulate a successful result with addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var resultSuccess = false
        var resultMessage: String? = null
        eventsDataMethods.addEventToUserFav(userId, eventId) { success, message ->
            resultSuccess = success
            resultMessage = message
        }

        // Assert
        assertTrue(resultSuccess)
        assertEquals("", resultMessage)
    }

    @Test
    fun removeEventFromUserFav() = runBlocking {
        // Arrange
        val userId = "user_123"
        val eventId = "event_456"
        val userFavEventsRef = mock<DocumentReference>()
        val mockTask = mock<Task<Void>>()
        val userDocumentReference:DocumentReference=mock()
        val favEventsCollectionReference:CollectionReference=mock()

        // Mock the Firestore collection and document reference
        whenever(firestore.collection("UserData")).thenReturn(collectionReference)
        whenever(collectionReference.document(userId)).thenReturn(userDocumentReference)
        whenever(userDocumentReference.collection("FavEvents")).thenReturn(favEventsCollectionReference)
        whenever(favEventsCollectionReference.document(eventId)).thenReturn(userFavEventsRef)
        whenever(userFavEventsRef.delete()).thenReturn(mockTask)

        // Simulate a successful result with addOnSuccessListener
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val successListener = invocation.arguments[0] as OnSuccessListener<Void>
            successListener.onSuccess(null)
            mockTask
        }

        // Act
        var resultSuccess = false
        var resultMessage: String? = null
        eventsDataMethods.removeEventFromUserFav(userId, eventId) { success, message ->
            resultSuccess = success
            resultMessage = message
        }

        // Assert
        assertTrue(resultSuccess)
        assertEquals("Event removed from favorites successfully.", resultMessage)
    }

}
