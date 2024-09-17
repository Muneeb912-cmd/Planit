package com.example.eventmanagement.workers.sync_pending_operarions

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import com.example.eventmanagement.repository.firebase.invites_data.InviteMethods
import com.example.eventmanagement.repository.firebase.user_data.UserDataMethods
import com.example.eventmanagement.repository.roomDb.Converters
import com.example.eventmanagement.repository.roomDb.PendingOperationDao
import com.example.eventmanagement.repository.roomDb.attendee_dao.AttendeeDao
import com.example.eventmanagement.repository.roomDb.events_dao.EventDao
import com.example.eventmanagement.repository.roomDb.fav_events_dao.FavEventsDao
import com.example.eventmanagement.repository.roomDb.invites_dao.InvitesDao
import com.example.eventmanagement.repository.roomDb.user_dao.UserDao
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncAttendeeDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncEventsDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncFavEventsDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncInvitesDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncUserDataWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltWorker
class SyncPendingOperationsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val userDao: UserDao,
    @Assisted private val eventDao: EventDao,
    @Assisted private val invitesDao: InvitesDao,
    @Assisted private val attendeeDao: AttendeeDao,
    @Assisted private val favEventsDao: FavEventsDao,
    @Assisted private val pendingOperations: PendingOperationDao,
    @Assisted private val connectivityObserver: ConnectivityObserver,
    @Assisted private val converters: Converters,
    @Assisted private val userDataMethods: UserDataMethods,
    @Assisted private val inviteMethods: InviteMethods,
    @Assisted private val eventDataMethods: EventDataMethods,
    @Assisted private val preferencesUtil: PreferencesUtil
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SyncPendingOperations", "doWork: SyncPendingOperationsTriggered")
                val operations = pendingOperations.getAll()
                Log.d("SyncPendingOperations", "doWork: $operations")
                for (operation in operations) {
                    if (connectivityObserver.isConnected) {
                        performOperationOnFirebase(operation)
                    } else {
                        performOperationLocally(operation)
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    private suspend fun performOperationOnFirebase(operation: PendingOperations) {
        when (operation.operationType) {
            OperationType.ADD -> addToFirebase(operation)
            OperationType.UPDATE -> updateFirebase(operation)
            OperationType.DELETE -> deleteFromFirebase(operation)
        }
    }

    private suspend fun performOperationLocally(operation: PendingOperations) {
        when (operation.operationType) {
            OperationType.ADD -> addToRoom(operation)
            OperationType.UPDATE -> updateRoom(operation)
            OperationType.DELETE -> deleteFromRoom(operation)
        }
    }

    private inline fun <reified T : androidx.work.Worker> enqueueWorker(tag: String) {
        val workRequest = OneTimeWorkRequest.Builder(T::class.java)
            .addTag(tag)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest)
    }

    private fun deleteOperation(operation: PendingOperations) {
        CoroutineScope(Dispatchers.IO).launch {
            pendingOperations.deleteOperation(operation)
        }
    }

    private suspend fun addToFirebase(operation: PendingOperations) {
        when (operation.dataType) {
            "event" -> {
                val event = converters.toEvent(operation.data)
                eventDataMethods.saveEvent(event) { result, _ ->
                    if (result) {
                        enqueueWorker<SyncEventsDataWorker>("SyncEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "invite" -> {
                val invite = converters.toInvite(operation.data)
                inviteMethods.createInvite(invite) { result ->
                    if (result) {
                        enqueueWorker<SyncInvitesDataWorker>("SyncInvitesDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "attendee" -> {
                val attendee = converters.toAttendee(operation.data)
                eventDataMethods.addAttendeeUpdatePeopleGoingCount(
                    attendee.eventId.toString(),
                    attendee.userId.toString()
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncAttendeeDataWorker>("SyncAttendeeDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "fav_event" -> {
                eventDataMethods.addEventToUserFav(
                    operation.userId,
                    operation.eventId,
                ) { result, _ ->
                    if (result) {
                        enqueueWorker<SyncFavEventsDataWorker>("SyncFavEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }
        }
    }

    private suspend fun updateFirebase(operation: PendingOperations) {
        when (operation.dataType) {
            "event" -> {
                val event = converters.toEvent(operation.data)
                eventDataMethods.updateEventById(event.eventId.toString(), event) { result ->
                    if (result) {
                        enqueueWorker<SyncEventsDataWorker>("SyncEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "user" -> {
                val user = converters.toUpdateUser(operation.data)
                userDataMethods.updateUserProfile(
                    user.userId.toString(),
                    user.userName.toString(),
                    user.userPhone.toString(),
                    user.userDob.toString(),
                    user.userImg.toString(),
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncEventsDataWorker>("SyncEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "user_location" -> {
                userDataMethods.updateUserLocation(operation.userId, operation.data) { result ->
                    if (result) {
                        enqueueWorker<SyncUserDataWorker>("SyncUserDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "user_profile_status" -> {
                userDataMethods.updateUserProfileStatus(
                    operation.userId,
                    operation.data.toBoolean()
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncUserDataWorker>("SyncUserDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "user_notification_status" -> {
                userDataMethods.updateUserNotificationSettings(
                    operation.userId,
                    operation.data.toBoolean()
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncUserDataWorker>("SyncUserDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "user_suspension_status" -> {
                userDataMethods.updateUserBanStatus(
                    operation.userId,
                    operation.data.toBoolean()
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncUserDataWorker>("SyncUserDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "event_invite" -> {
                inviteMethods.updateInviteStatus(
                    operation.documentId,
                    operation.data
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncInvitesDataWorker>("SyncInvitesDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "reject_event_invite" -> {
                inviteMethods.updateInviteStatusByUserId(
                    operation.userId,
                    operation.eventId,
                    operation.data
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncInvitesDataWorker>("SyncInvitesDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "resend_invite" -> {
                inviteMethods.updateInviteStatusByUserId(
                    operation.userId,
                    operation.eventId,
                    operation.data
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncInvitesDataWorker>("SyncInvitesDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "event_status" -> {
                eventDataMethods.updateEventStatusById(
                    operation.documentId,
                    operation.data
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncFavEventsDataWorker>("SyncFavEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

        }
    }

    private suspend fun deleteFromFirebase(operation: PendingOperations) {
        when (operation.dataType) {
            "event" -> {
                val event = converters.toEvent(operation.data)
                eventDataMethods.deleteEventById(event.eventId.toString(), true) { result ->
                    if (result) {
                        enqueueWorker<SyncEventsDataWorker>("SyncEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "invite" -> {
                val invite = converters.toInvite(operation.data)
                inviteMethods.deleteInvite(
                    invite.eventId.toString(),
                    invite.receiverId.toString()
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncInvitesDataWorker>("SyncInvitesDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "fav_event" -> {
                eventDataMethods.removeEventFromUserFav(
                    operation.userId,
                    operation.eventId
                ) { result, _ ->
                    if (result) {
                        enqueueWorker<SyncFavEventsDataWorker>("SyncFavEventsDataWorker")
                        deleteOperation(operation)
                    }
                }
            }

            "attendee" -> {
                eventDataMethods.removeAttendeeUpdatePeopleGoingCount(
                    operation.eventId,
                    operation.userId
                ) { result ->
                    if (result) {
                        enqueueWorker<SyncAttendeeDataWorker>("SyncAttendeeDataWorker")
                        deleteOperation(operation)
                    }
                }
            }
        }
    }

    private suspend fun addToRoom(operation: PendingOperations) {
        when (operation.dataType) {
            "user" -> {
                val user = converters.toUser(operation.data)
                userDao.saveUserData(user)
            }

            "event" -> {
                val event = converters.toEvent(operation.data)
                eventDao.saveEvent(event)
            }

            "invite" -> {
                val invite = converters.toInvite(operation.data)
                invitesDao.createInvite(invite)
            }

            "attendee" -> {
                val attendee = converters.toAttendee(operation.data)
                attendeeDao.addAttendee(attendee)
            }

            "fav_event" -> {
                val favEvent = converters.toFavEvent(operation.data)
                favEventsDao.addEventToUserFav(favEvent)
            }
        }
    }

    private suspend fun updateRoom(operation: PendingOperations) {
        when (operation.dataType) {
            "user" -> {
                val user = converters.toUser(operation.data)
                userDao.updateUserProfile(
                    user.userId.toString(),
                    user.userName.toString(),
                    user.userPhone.toString(),
                    user.userDob.toString(),
                    user.userImg.toString()
                )
            }

            "event" -> {
                val event = converters.toEvent(operation.data)
                eventDao.updateEventById(
                    eventTitle = event.eventTitle,
                    eventOrganizer = event.eventOrganizer,
                    eventTiming = event.eventTiming,
                    eventCategory = event.eventCategory,
                    eventDescription = event.eventDescription,
                    eventLocation = event.eventLocation,
                    eventLong = event.eventLong,
                    eventLat = event.eventLat,
                    eventDate = event.eventDate,
                    isEventFeatured = event.isEventFeatured,
                    isEventPopular = event.isEventPopular,
                    numberOfPeopleAttending = event.numberOfPeopleAttending,
                    isEventPublic = event.isEventPublic,
                    eventStatus = event.eventStatus,
                    eventCreatedBy = event.eventCreatedBy,
                    isEventDeleted = event.isEventDeleted,
                    eventId = event.eventId.toString()
                )
            }

            "user_profile_status" -> {
                userDao.updateUserProfileStatus(
                    operation.userId,
                    operation.data.toBoolean()
                )
            }

            "user_location" -> {
                userDao.updateUserLocation(
                    operation.userId,
                    operation.data
                )
            }

            "user_notification_status" -> {
                userDao.updateUserNotificationSettings(
                    operation.userId,
                    operation.data.toBoolean()
                )
            }

            "user_suspension_status" -> {
                userDao.updateUserBanStatus(
                    operation.userId,
                    operation.data.toBoolean()
                )
            }

            "event_invite" -> {
                val invite = converters.toInvite(operation.data)
                invitesDao.updateInviteStatus(
                    invite.inviteId.toString(),
                    invite.inviteStatus.toString()
                )
            }

            "event_status" -> {
                invitesDao.updateInviteStatus(
                    operation.documentId,
                    operation.data
                )
            }
        }
    }

    private suspend fun deleteFromRoom(operation: PendingOperations) {
        when (operation.dataType) {
            "user" -> {
                userDao.deleteUser(operation.userId)
            }

            "event" -> {
                eventDao.deleteEventById(operation.eventId)
            }

            "invite" -> {
                invitesDao.deleteInvite(operation.eventId, operation.userId)
            }

            "attendee" -> {
                attendeeDao.removeAttendee(operation.userId,operation.eventId)
            }

            "fav_event" -> {
                Log.d(
                    "FavEventToBeRemoved",
                    "deleteFromRoom: ${operation.userId} ${operation.eventId}"
                )
                favEventsDao.removeEventFromUserFav(operation.userId, operation.eventId)
            }
        }
    }
}
