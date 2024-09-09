package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.add_edit_event

import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.repository.room_db.Converters
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import com.example.eventmanagement.utils.Validators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AddEditEventViewModelTest {

    private lateinit var viewModel: AddEditEventViewModel
    private val validators: Validators = mock()
    private val connectivityObserver: ConnectivityObserver = mock()
    private val pendingOperationDao: PendingOperationDao = mock()
    private val converters: Converters = mock()

    @BeforeEach
    fun setUp() {
        viewModel = AddEditEventViewModel(
            validators = validators,
            connectivityObserver = connectivityObserver,
            pendingOperationDao = pendingOperationDao,
            converters = converters
        )
    }

    @Test
    fun `test updateEventInfo updates event data`() = runTest {
        // Given
        val key = "eventTitle"
        val value = "New Event Title"

        // When
        viewModel.updateEventInfo(key, value)

        // Then
        val updatedEvent = viewModel.eventsData.first()
        assertEquals("New Event Title", updatedEvent.eventTitle)
    }

    @Test
    fun `test saveEvent triggers pending operation`() = runTest {
        // Given
        val eventData = EventData(
            eventId = "1",
            eventTitle = "Test Event",
            eventCreatedBy = "user123"
        )
        whenever(converters.fromEvent(any())).thenReturn("json_data")

        viewModel.updateEventInfo("eventId", eventData.eventId.toString())
        viewModel.updateEventInfo("eventTitle", eventData.eventTitle.toString())
        viewModel.updateEventInfo("eventCreatedBy", eventData.eventCreatedBy.toString())

        // When
        viewModel.saveEvent()

        // Then
        verify(pendingOperationDao).insert(
            argThat { operation ->
                operation.documentId == "1" &&
                        operation.data == "json_data" &&
                        operation.operationType == OperationType.ADD &&
                        operation.userId == "user123"
            }
        )
    }

    @Test
    fun `test updateEventInfo validates fields correctly`() = runTest {
        // Given
        whenever(validators.validateName("Valid Title")).thenReturn(true)
        whenever(validators.validateName("")).thenReturn(false)

        // When: Valid title
        viewModel.updateEventInfo("eventTitle", "Valid Title")
        val errorsAfterValid = viewModel.errors.first()

        // Then: Valid title should not produce an error
        assertNull(errorsAfterValid["eventTitle"], "Valid title should not produce an error")

        // When: Invalid title
        viewModel.updateEventInfo("eventTitle", "")
        val errorsAfterInvalid = viewModel.errors.first()

        // Then: Empty title should produce an error
        assertEquals("Invalid Title. Example Input: (Promotion Ceremony)", errorsAfterInvalid["eventTitle"], "Empty title should produce an error")
    }

}
