package com.example.eventmanagement.ui.fragments.events

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.FavEvents
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.room_db.Converters
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.check
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EventsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EventsViewModel
    private val pendingOperationDao: PendingOperationDao = mock()
    private val converters: Converters = mock()

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EventsViewModel(pendingOperationDao, converters)
    }

//    @Test
//    fun `addEventToUserFav should add event to user favorites`() = runBlocking {
////        // Given
////        val userId = "user123"
////        val eventData = EventData(eventId = "1")
////        val favEvents = FavEvents(userId = userId, eventId = eventData.eventId.toString())
////        val jsonData = "jsonData"
////
////        // Mock behavior
////        whenever(converters.fromFavEvent(favEvents)).thenReturn(jsonData)
////        whenever(pendingOperationDao.countByDocumentId(
////            eventData.eventId.toString(),
////            "DELETE",
////            "fav_event"
////        )).thenReturn(0) // Simulate that there are no pending "DELETE" operations
////
////        // When
////        viewModel.addEventToUserFav(userId, eventData) { success, message ->
////            assertTrue(success)
////            assertEquals("All Good", message)
////        }
////
////        // Then
////        verify(pendingOperationDao, times(1)).countByDocumentId(
////            eventData.eventId.toString(),
////            "DELETE",
////            "fav_event"
////        )
////        verify(pendingOperationDao, times(0)).delete(userId, eventData.eventId.toString(), "fav_event") // Should not be called
////        verify(pendingOperationDao, times(1)).insert(
////            check { operation ->
////                assertNotNull(operation) // Ensure it is not null
////                assertEquals(OperationType.ADD, operation.operationType)
////                assertEquals(eventData.eventId.toString(), operation.documentId)
////                assertEquals(jsonData, operation.data)
////                assertEquals(userId, operation.userId)
////                assertEquals("fav_event", operation.dataType)
////            }
////        )
//    }


//    @Test
//    fun `removeEventFromUserFav should remove event from user favorites`() = runBlocking {
//        // Given
//        val userId = "user123"
//        val eventData = EventData(eventId = "1")
//        val event = "eventJson"
//
//        // Mock behavior
//        whenever(converters.fromEvent(eventData)).thenReturn(event) // Ensure this is non-null
//        whenever(pendingOperationDao.countByDocumentId(
//            eventData.eventId.toString(),
//            "ADD",
//            "fav_event"
//        )).thenReturn(0)
//
//        // When
//        viewModel.removeEventFromUserFav(userId, eventData) { success, message ->
//            assertTrue(success)
//            assertEquals("All Good", message)
//        }
//
//        // Then
//        verify(pendingOperationDao, times(1)).insert(
//            check { operation ->
//                assertNotNull(operation) // Ensure it is not null
//                assertEquals(OperationType.DELETE, operation.operationType)
//                assertEquals(eventData.eventId.toString(), operation.eventId)
//                assertEquals(userId, operation.userId)
//                assertEquals("fav_event", operation.dataType)
//                assertEquals(event, operation.data) // Ensure this is non-null
//            }
//        )
//    }


}
