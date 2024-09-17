package com.example.eventmanagement.ui.activities.fav_events

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao

@OptIn(ExperimentalCoroutinesApi::class)
class FavEventsViewModelTest {

    private lateinit var viewModel: FavEventsViewModel
    private val converters: Converters = mock()
    private val pendingOperationDao: PendingOperationDao = mock()

    @BeforeEach
    fun setup() {
        viewModel = FavEventsViewModel(converters, pendingOperationDao)
    }

    @Test
    fun `removeEventFromUserFav should insert delete operation or update existing operation`() = runTest {
//        // Given
//        val userId = "user123"
//        val eventData = EventData(eventId = "event456", /* other properties if needed */)
//        val event = "eventJson" // Expected JSON representation of eventData
//        whenever(converters.fromEvent(eventData)).thenReturn(event)
//        whenever(pendingOperationDao.countByDocumentId(eventData.eventId.toString(), "DELETE", "fav_event"))
//            .thenReturn(0) // Adjust as needed for the test
//
//        // When
//        val result = mutableListOf<Boolean>()
//        viewModel.removeEventFromUserFav(userId, eventData) { success, message ->
//            result.add(success)
//            assertEquals("Operation completed", message)
//        }
//
//        // Ensure all coroutines are completed
//        advanceUntilIdle()
//
//        // Then
//        val captor = argumentCaptor<PendingOperations>()
//        verify(pendingOperationDao).insert(captor.capture())
//
//        val capturedOperation = captor.firstValue
//        assertEquals(OperationType.DELETE, capturedOperation.operationType)
//        assertEquals(userId, capturedOperation.documentId)
//        assertEquals(event, capturedOperation.data)
//        assertEquals(userId, capturedOperation.userId)
//        assertEquals(eventData.eventId.toString(), capturedOperation.eventId)
//        assertEquals("fav_event", capturedOperation.dataType)
    }
}
