package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile

import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.ui.bottomsheets.ediitprofile.EditProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {

    @Mock
    private lateinit var pendingOperationDao: PendingOperationDao

    @Mock
    private lateinit var converters: Converters

    private lateinit var viewModel: EditProfileViewModel
    private lateinit var testDispatcher: TestDispatcher

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        viewModel = EditProfileViewModel(pendingOperationDao, converters)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

//    @Test
//    fun `updateUserData should insert or update pending operation`() = runTest {
//        // Given
//        val userId = "user123"
//        val userName = "John Doe"
//        val userPhone = "123456789"
//        val userDob = "01-01-1990"
//        val userImg = "profile.jpg"
//        val jsonData = "{}"
//
//        whenever(converters.fromUpdateUser(any())).thenReturn(jsonData)
//        whenever(pendingOperationDao.countByDocumentId(userId, "UPDATE", "user")).thenReturn(0)
//
//        // When
//        viewModel.updateUserData(userId, userName, userPhone, userDob, userImg)
//
//        // Then
//        verify(pendingOperationDao).insert(
//            PendingOperations(
//                operationType = OperationType.UPDATE,
//                documentId = userId,
//                data = jsonData,
//                userId = userId,
//                eventId = "",
//                dataType = "user"
//            )
//        )
//
//        val state = viewModel.editDataStates.first()
//        assertTrue(state is Response.Success)
//    }

//    @Test
//    fun `updateUserBanStatus should insert or update pending operation`() = runTest {
//        // Given
//        val userId = "user123"
//        val banStatus = true
//
//        whenever(pendingOperationDao.countByDocumentId(userId, "UPDATE", "user_suspension_status"))
//            .thenReturn(0)
//
//        val expectedPendingOperation = PendingOperations(
//            operationType = OperationType.UPDATE,
//            documentId = userId,
//            data = banStatus.toString(),
//            userId = userId,
//            eventId = "",
//            dataType = "user_suspension_status"
//        )
//
//        // When
//        val result = mutableListOf<Boolean>()
//        viewModel.updateUserBanStatus(userId, banStatus) {
//            result.add(it)
//        }
//
//        // Ensure all coroutines are completed
//        advanceUntilIdle()
//
//        // Then
//        verify(pendingOperationDao).insert(expectedPendingOperation)
//
//        // Verify the result callback was triggered with true
//        assertEquals(1, result.size)
//        assertTrue(result.first())
//    }
}

