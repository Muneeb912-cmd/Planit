package com.example.eventmanagement.ui.fragments.manageevents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach

import org.mockito.Mockito

@ExperimentalCoroutinesApi
class EventManagementViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val pendingOperationDao: PendingOperationDao =
        Mockito.mock(PendingOperationDao::class.java)
    private val converters: Converters = Mockito.mock(Converters::class.java)

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: EventManagementViewModel
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EventManagementViewModel(pendingOperationDao, converters)
    }

//    @Test
//    fun deleteEventById() = runTest {
//        // Arrange
//        val eventData = EventData(0, "event123")
//        val event = "eventJson"
//
//        // Mocking converters
//        Mockito.`when`(converters.fromEvent(eventData)).thenReturn(event)
//
//        // Act
//        viewModel.deleteEventById(eventData, true) { success ->
//            // Assert
//            assertTrue(success)
//        }
//
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Verify that insert was called once with the correct parameters
//        verify(pendingOperationDao, times(1)).insert(
//            check { operation ->
//                assertNotNull(operation) // Ensure it is not null
//                assertEquals(OperationType.DELETE, operation.operationType)
//                assertEquals(eventData.eventId.toString(), operation.documentId)
//                assertEquals("event", operation.dataType)
//                assertEquals(event, operation.data)
//            }
//        )
//    }


}