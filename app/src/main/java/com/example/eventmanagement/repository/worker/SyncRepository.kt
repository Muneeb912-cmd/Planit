package com.example.eventmanagement.repository.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.eventmanagement.workers.sync_pending_operarions.SyncPendingOperationsWorker
import javax.inject.Inject

class SyncRepository @Inject constructor(
    private val workManager: WorkManager
) {
    fun scheduleSyncPendingOperationsWorker() {
        val workRequest = OneTimeWorkRequest.Builder(SyncPendingOperationsWorker::class.java)
            .build()
        workManager.enqueueUniqueWork(
            "sync_pending_operations",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}