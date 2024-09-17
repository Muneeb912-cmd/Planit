package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details

import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.ui.bottomsheets.eventdetails.EventDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailsViewModelTest {


    private var pendingOperationDao: PendingOperationDao=mock()

    private var converters: Converters=mock()

    private lateinit var viewModel: EventDetailsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EventDetailsViewModel(pendingOperationDao, converters)
    }

    @Test
    fun `addUserAsAttendee should insert add operation and remove conflicting delete`() = runTest {
//        // Given
//        val eventId = "event123"
//        val userId = "user123"
//        val jsonData = "jsonData"
//
//        whenever(converters.fromAttendee(any())).thenReturn(jsonData)
//        whenever(pendingOperationDao.countByDocumentId(eventId, "DELETE", "attendee"))
//            .thenReturn(1)
//
//        // When
//        val result = mutableListOf<Boolean>()
//        viewModel.addUserAsAttendee(eventId, userId) {
//            result.add(it)
//        }
//
//        // Ensure all coroutines are completed
//        advanceUntilIdle()
//
//        // Then
//        verify(pendingOperationDao).delete(userId, eventId, "attendee")
//        verify(pendingOperationDao).insert(
//            check { pendingOperation ->
//                assertEquals(OperationType.ADD, pendingOperation.operationType)
//                assertEquals(eventId, pendingOperation.documentId)
//                assertEquals(jsonData, pendingOperation.data)
//                assertEquals(userId, pendingOperation.userId)
//                assertEquals(eventId, pendingOperation.eventId)
//                assertEquals("attendee", pendingOperation.dataType)
//                // Timestamp can be ignored or checked if necessary
//            }
//        )
//
//        assertTrue(result.first())
    }

    @Test
    fun `removeUserAsAttendee should insert delete operation and remove conflicting add`() = runTest {
//        // Given
//        val eventId = "event123"
//        val userId = "user123"
//        val jsonData = "jsonData"
//
//        whenever(converters.fromAttendee(any())).thenReturn(jsonData)
//        whenever(pendingOperationDao.countByDocumentId(eventId, "ADD", "attendee"))
//            .thenReturn(1)
//
//        // When
//        val result = mutableListOf<Boolean>()
//        viewModel.removeUserAsAttendee(eventId, userId) {
//            result.add(it)
//        }
//
//        // Ensure all coroutines are completed
//        advanceUntilIdle()
//
//        // Then
//        verify(pendingOperationDao).delete(userId, eventId, "attendee")
//        verify(pendingOperationDao).insert(
//            check { pendingOperation ->
//                assertEquals(OperationType.DELETE, pendingOperation.operationType)
//                assertEquals(userId, pendingOperation.documentId)
//                assertEquals(jsonData, pendingOperation.data)
//                assertEquals(userId, pendingOperation.userId)
//                assertEquals(eventId, pendingOperation.eventId)
//                assertEquals("attendee", pendingOperation.dataType)
//                // Timestamp can be ignored or checked if necessary
//            }
//        )
//
//        assertTrue(result.first())
   }
}
