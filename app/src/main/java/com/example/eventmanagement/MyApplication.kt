package com.example.eventmanagement

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.eventmanagement.receivers.ConnectivityObserver
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
import com.example.eventmanagement.service.EventNotificationService
import com.example.eventmanagement.utils.PreferencesUtil
import com.example.eventmanagement.workers.sync_pending_operarions.SyncPendingOperationsWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    override fun onCreate() {
        super.onCreate()
        scheduleSyncPendingOperationsWorker()
        val intent = Intent(this, EventNotificationService::class.java)
        ContextCompat.startForegroundService(this, intent)

    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleSyncPendingOperationsWorker() {
        val workRequest = OneTimeWorkRequest.Builder(SyncPendingOperationsWorker::class.java)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "sync_pending_operations",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

}

class CustomWorkerFactory @Inject constructor(
    private val userDao: UserDao,
    private val eventDao: EventDao,
    private val invitesDao: InvitesDao,
    private val attendeeDao: AttendeeDao,
    private val favEventsDao: FavEventsDao,
    private val pendingOperations: PendingOperationDao,
    private val connectivityObserver: ConnectivityObserver,
    private val converters: Converters,
    private val userDataMethods: UserDataMethods,
    private val inviteMethods: InviteMethods,
    private val eventDataMethods: EventDataMethods,
    private val preferencesUtil: PreferencesUtil
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? =
        when (workerClassName) {
            SyncPendingOperationsWorker::class.java.name -> {
                SyncPendingOperationsWorker(
                    appContext,
                    workerParameters,
                    userDao,
                    eventDao,
                    invitesDao,
                    attendeeDao,
                    favEventsDao,
                    pendingOperations,
                    connectivityObserver,
                    converters,
                    userDataMethods,
                    inviteMethods,
                    eventDataMethods,
                    preferencesUtil
                )
            }

            else -> {
                null
            }
        }

}
