package com.example.eventmanagement.ui.activities.event_invites

import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.argumentCaptor

@OptIn(ExperimentalCoroutinesApi::class)
class EventInviteViewModelTest {

    private lateinit var viewModel: EventInviteViewModel
    private val pendingOperationDao: PendingOperationDao = mock()

    @BeforeEach
    fun setup() {
        viewModel = EventInviteViewModel(pendingOperationDao)
    }

    @Test
    fun `updateInviteStatus should insert update operation into DAO`() = runTest {
        // Given
        val inviteId = "invite123"
        val newStatus = "accepted"

        // When
        val result = mutableListOf<Boolean>()
        viewModel.updateInviteStatus(inviteId, newStatus) { success, message ->
            result.add(success)
            assertEquals("All Good!", message)
        }

        // Ensure all coroutines are completed
        advanceUntilIdle()

        // Then
        val captor = argumentCaptor<PendingOperations>()
        verify(pendingOperationDao).insert(captor.capture())

        val capturedOperation = captor.firstValue
        assertEquals(OperationType.UPDATE, capturedOperation.operationType)
        assertEquals(inviteId, capturedOperation.documentId)
        assertEquals(newStatus, capturedOperation.data)
        assertEquals("", capturedOperation.userId) // as per implementation
        assertEquals("", capturedOperation.eventId) // as per implementation
        assertEquals("event_invite", capturedOperation.dataType)

        assertTrue(result.first())
    }
}
