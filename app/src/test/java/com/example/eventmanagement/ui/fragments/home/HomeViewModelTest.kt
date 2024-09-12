package com.example.eventmanagement.ui.fragments.home

import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.FavEvents
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.room_db.Converters
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.check

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {

    private var viewModel: HomeViewModel=mock()
    private var pendingOperationDao: PendingOperationDao=mock()
    private var converters: Converters=mock()

    private val testDispatcher = StandardTestDispatcher()

    private val pendingOperationsCaptor: ArgumentCaptor<PendingOperations> =
        ArgumentCaptor.forClass(PendingOperations::class.java)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        pendingOperationDao = mock(PendingOperationDao::class.java)
        converters = mock(Converters::class.java)
        viewModel = HomeViewModel(pendingOperationDao, converters)
    }

//    @Test
//    fun `addEventToUserFav adds event to user favorites`() = runTest {
////        val userId = "user123"
////        val eventData = EventData(eventId = "event123")
////        val favEvents = FavEvents(userId = userId, eventId = eventData.eventId)
////        val jsonData = "someJsonData"
////
////        `when`(converters.fromFavEvent(favEvents)).thenReturn(jsonData)
////        `when`(pendingOperationDao.countByDocumentId(eventData.eventId.toString(), "DELETE", "fav_event"))
////            .thenReturn(0)
////
////        viewModel.addEventToUserFav(userId, eventData) { success, message ->
////            assertTrue(success)
////            assertEquals("All Good", message)
////        }
////
////        testDispatcher.scheduler.advanceUntilIdle()
////
////        // Verify that insert was called once
////        verify(pendingOperationDao, times(1)).insert(
////            check { operation ->
////                assertEquals(OperationType.ADD, operation.operationType)
////                assertEquals(eventData.eventId.toString(), operation.documentId)
////                assertEquals(userId, operation.userId)
////                assertEquals("fav_event", operation.dataType)
////                assertEquals(jsonData, operation.data)
////            }
////        )
//    }

//    @Test
//    fun `removeEventFromUserFav removes event from user favorites`() = runTest {
//        val userId = "user123"
//        val eventData = EventData(eventId = "event123")
//        val eventJson = "someJsonData"
//
//        `when`(converters.fromEvent(eventData)).thenReturn(eventJson)
//        `when`(pendingOperationDao.countByDocumentId(eventData.eventId.toString(), "ADD", "fav_event"))
//            .thenReturn(0)
//
//        viewModel.removeEventFromUserFav(userId, eventData) { success, message ->
//            assertTrue(success)
//            assertEquals("All Good", message)
//        }
//
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Verify that insert was called once
//        verify(pendingOperationDao, times(1)).insert(
//            check { operation ->
//                assertEquals(OperationType.DELETE, operation.operationType)
//                assertEquals(userId, operation.documentId)
//                assertEquals(userId, operation.userId)
//                assertEquals("fav_event", operation.dataType)
//                assertEquals(eventJson, operation.data)
//            }
//        )
//    }
}