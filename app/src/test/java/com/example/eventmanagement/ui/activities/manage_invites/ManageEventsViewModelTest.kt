package com.example.eventmanagement.ui.activities.manage_invites

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao

@OptIn(ExperimentalCoroutinesApi::class)
class ManageEventsViewModelTest {

    private lateinit var viewModel: ManageEventsViewModel
    private val pendingOperationDao: PendingOperationDao = mock()
    private val converters: Converters = mock()

    @BeforeEach
    fun setup() {
        viewModel = ManageEventsViewModel(pendingOperationDao, converters)
    }

//    @Test
//    fun `createInvite should insert add operation`() = runTest {
//        // Given
//        val eventId = "event1"
//        val currentUserId = "user1"
//        val userData = User.UserData(userId = "user2")
//        val invite = Invites(
//            eventId = eventId,
//            inviteStatus = "pending",
//            senderId = currentUserId,
//            receiverId = userData.userId,
//            inviteTime = viewModel.getCurrentFormattedTimestamp()
//        )
//
//        whenever(converters.fromInvite(invite)).thenReturn("jsonData")
//
//        // When
//        viewModel.createInvite(eventId, currentUserId, userData) { result ->
//            // Then
//            assertTrue(result) // Check if the result is true
//
//            verify(pendingOperationDao).insert(
//                PendingOperations(
//                    operationType = OperationType.ADD,
//                    documentId = invite.inviteId.toString(),
//                    data = "jsonData",
//                    userId = invite.receiverId.toString(),
//                    eventId = invite.eventId.toString(),
//                    dataType = "invite"
//                )
//            )
//        }
//    }

//    @Test
//    fun `deleteInvite should insert delete operation`() = runTest {
//        // Given
//        val eventId = "event1"
//        val currentUserId = "user1"
//        val userData = User.UserData(userId = "user2")
//        val invite = Invites(
//            eventId = eventId,
//            inviteStatus = "pending",
//            senderId = currentUserId,
//            receiverId = userData.userId,
//            inviteTime = viewModel.getCurrentFormattedTimestamp()
//        )
//
//        whenever(converters.fromInvite(invite)).thenReturn("jsonData")
//
//        // When
//        viewModel.deleteInvite(eventId, currentUserId, userData) { result ->
//            // Then
//            assertTrue(result) // Check if the result is true
//
//            verify(pendingOperationDao).insert(
//                PendingOperations(
//                    operationType = OperationType.DELETE,
//                    documentId = invite.inviteId.toString(),
//                    data = "jsonData",
//                    userId = invite.receiverId.toString(),
//                    eventId = invite.eventId.toString(),
//                    dataType = "invite"
//                )
//            )
//        }
//    }
}
