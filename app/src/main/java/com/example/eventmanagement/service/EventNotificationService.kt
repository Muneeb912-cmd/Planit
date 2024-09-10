package com.example.eventmanagement.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.fragment.app.activityViewModels
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import com.example.eventmanagement.repository.room_db.events_dao.EventDao
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class EventNotificationService : Service() {

    @Inject
    lateinit var eventDao: EventDao

    @Inject
    lateinit var pendingOperationDao: PendingOperationDao

    @Inject
    lateinit var eventDataMethods: EventDataMethods

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    enum class NotificationType {
        REMINDER_15_MIN, START_EVENT, ENDING_20_MIN, END_EVENT
    }

    data class EventQueueItem(
        val event: EventData,
        val triggerTime: LocalTime,
        val notificationType: NotificationType
    )

    private val eventQueue = ArrayList<EventQueueItem>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d("EventNotificationService", "Service Created")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(1, createForegroundNotification())
        coroutineScope.launch { observeEventsAndUpdateQueue() }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No binding necessary
    }

    // In your service class
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun observeEventsAndUpdateQueue() {
        try {
            eventDao.observeAllEvents().collect { events ->
                Log.d("EventNotificationService", "observeEventsAndUpdateQueue: $events")
                eventQueue.clear()
                for (event in events) {
                    addEventToQueue(event)
                }
                eventQueue.sortBy { it.triggerTime }
                processNextEvent()
            }
        } catch (e: Exception) {
            Log.e("EventNotificationService", "Error observing events: ${e.message}")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun processNextEvent() {
        if (eventQueue.isNotEmpty()) {
            eventQueue.sortBy { it.triggerTime }

            val nextEvent = eventQueue.first()
            val now = LocalTime.now()
            val delayTime = ChronoUnit.MILLIS.between(now, nextEvent.triggerTime)

            val minutesUntilNextEvent = ChronoUnit.MINUTES.between(now, nextEvent.triggerTime)
            Log.d(
                "EventNotificationService",
                "Next event '${nextEvent.event.eventTitle}' will trigger in $minutesUntilNextEvent minutes at ${nextEvent.triggerTime}"
            )

            if (delayTime > 0) {
                delay(delayTime)  // Delay until the next event's trigger time
            }

            triggerNotification(nextEvent.event, nextEvent.notificationType)
            updateEventStatus(nextEvent.event, nextEvent.notificationType)

            eventQueue.remove(nextEvent)

            processNextEvent()
        }
    }


    private suspend fun updateEventStatus(event: EventData, notificationType: NotificationType) {
        when (notificationType) {
            NotificationType.START_EVENT -> {
                val pendingOperation = PendingOperations(
                    operationType = OperationType.UPDATE,
                    documentId = event.eventId.toString(),
                    data = "On-Going",
                    userId = "",
                    eventId = event.eventId.toString(),
                    dataType = "event_status"
                )
                pendingOperationDao.insert(pendingOperation)
            }

            NotificationType.ENDING_20_MIN -> {}
            NotificationType.END_EVENT -> {
                val pendingOperation = PendingOperations(
                    operationType = OperationType.UPDATE,
                    documentId = event.eventId.toString(),
                    data = "Missed",
                    userId = "",
                    eventId = event.eventId.toString(),
                    dataType = "event_status"
                )
                pendingOperationDao.insert(pendingOperation)
            }

            NotificationType.REMINDER_15_MIN -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(timeString: String): LocalTime {
        val timeStringTrimmed = timeString.trim()
        val formatter12Hour = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")

        return try {
            LocalTime.parse(timeStringTrimmed, formatter12Hour)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid time format: $timeStringTrimmed")
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun addEventToQueue(event: EventData) {
        val (startTimeString, endTimeString) = event.eventTiming.toString().split(" - ")
        val startTime = parseTime(startTimeString)
        val endTime = parseTime(endTimeString)

        val now = LocalTime.now()

        Log.d(
            "EventNotificationService",
            "Current Time: $now, Start Time: $startTime, End Time: $endTime"
        )

        // Comparison should happen in 12-hour format
        if (now.isBefore(startTime)) {
            eventQueue.add(
                EventQueueItem(
                    event,
                    startTime.minusMinutes(15),
                    NotificationType.REMINDER_15_MIN
                )
            )
            eventQueue.add(EventQueueItem(event, startTime, NotificationType.START_EVENT))
        }

        if (now.isBefore(endTime)) {
            eventQueue.add(
                EventQueueItem(
                    event,
                    endTime.minusMinutes(20),
                    NotificationType.ENDING_20_MIN
                )
            )
            eventQueue.add(EventQueueItem(event, endTime, NotificationType.END_EVENT))
        }
    }


    private fun triggerNotification(event: EventData, notificationType: NotificationType) {
        val notificationText = when (notificationType) {
            NotificationType.REMINDER_15_MIN -> "Event '${event.eventTitle}' starts in 15 minutes."
            NotificationType.START_EVENT -> "Event '${event.eventTitle}' is starting now!"
            NotificationType.ENDING_20_MIN -> "Event '${event.eventTitle}' is ending in 20 minutes."
            NotificationType.END_EVENT -> "Event '${event.eventTitle}' has ended."
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "event_channel")
            .setContentTitle("PlanIt")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.splash_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(event.eventId.hashCode(), notification)
    }


    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, "event_channel")
            .setContentTitle("Event Notification Service")
            .setContentText("Service is running in the background.")
            .setSmallIcon(R.drawable.splash_logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "event_channel",
            "Event Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for event notifications"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("EventNotificationService", "Service Destroyed")
        coroutineScope.cancel()
    }
}
