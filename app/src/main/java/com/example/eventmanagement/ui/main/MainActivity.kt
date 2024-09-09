package com.example.eventmanagement.ui.main
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.eventmanagement.R
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncAttendeeDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncEventsDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncFavEventsDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncInvitesDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncUserDataWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)
    }
}