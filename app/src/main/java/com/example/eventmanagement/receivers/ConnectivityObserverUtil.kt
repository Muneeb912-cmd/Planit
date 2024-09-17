package com.example.eventmanagement.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.eventmanagement.repository.roomDb.LocalDB
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncAttendeeDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncEventsDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncFavEventsDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncInvitesDataWorker
import com.example.eventmanagement.workers.sync_data_from_firebase.SyncUserDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectivityObserver(private val context: Context) {

    private val connectivityListeners = mutableSetOf<ConnectivityListener>()
    var isConnected = checkConnectivity()
    private val database: LocalDB = LocalDB.getInstance(context)
    private val pendingOperationDao = database.pendingOperationsDao()

    interface ConnectivityListener {
        fun onConnectivityChanged(isConnected: Boolean)
    }

    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val pendingOperations = pendingOperationDao.getAll()
                    val newIsConnected = checkConnectivity()
                    if (newIsConnected != isConnected) {
                        isConnected = newIsConnected
                        withContext(Dispatchers.Main) {
                            notifyListeners()
                        }
                        if (isConnected && pendingOperations.isEmpty()) {
                            triggerSyncWorkers()
                            Log.d("Worker", "onReceive: workers triggered")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ConnectivityReceiver", "Error handling connectivity change", e)
                }
            }
        }
    }


    private fun triggerSyncWorkers() {
        enqueueWorker<SyncUserDataWorker>("SyncUserDataWorker")
        enqueueWorker<SyncEventsDataWorker>("SyncEventsDataWorker")
        enqueueWorker<SyncInvitesDataWorker>("SyncInvitesDataWorker")
        enqueueWorker<SyncAttendeeDataWorker>("SyncAttendeeDataWorker")
        enqueueWorker<SyncFavEventsDataWorker>("SyncFavEventsDataWorker")
    }

    private inline fun <reified T : androidx.work.Worker> enqueueWorker(tag: String) {
        val workRequest = OneTimeWorkRequest.Builder(T::class.java)
            .addTag(tag)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(tag, ExistingWorkPolicy.REPLACE, workRequest)
    }

    init {
        registerReceiver()
    }

    private fun registerReceiver() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(connectivityReceiver, filter)
        isConnected = checkConnectivity()
    }

    private fun checkConnectivity(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    fun registerListener(listener: ConnectivityListener) {
        connectivityListeners.add(listener)
    }

    fun unregisterListener(listener: ConnectivityListener) {
        connectivityListeners.remove(listener)
    }

    private fun notifyListeners() {
        connectivityListeners.forEach { listener ->
            listener.onConnectivityChanged(isConnected)
        }
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(connectivityReceiver)
    }
}
