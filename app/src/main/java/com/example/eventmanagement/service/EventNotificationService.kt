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
import com.example.eventmanagement.R
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.repository.room_db.events_dao.EventDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class EventNotificationService : Service() {

    @Inject
    lateinit var eventDao: EventDao

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d("EventNotificationService", "Service Created")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(1, createForegroundNotification())
        startNotificationLoop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // No binding necessary
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseTime(timeString: String): LocalTime {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return LocalTime.parse(timeString, formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isEventActive(startTime: String, endTime: String): Boolean {
        val currentTime = LocalTime.now()
        val start = parseTime(startTime)
        val end = parseTime(endTime)
        Log.d("EventTiming", "isEventActive: ${currentTime.isAfter(start) && currentTime.isBefore(end)}")
        return currentTime.isAfter(start) && currentTime.isBefore(end)
    }

    private fun createNotification(event: EventData) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "event_channel")
            .setContentTitle("Event Reminder")
            .setContentText("Event '${event.eventTitle}' is active now!")
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
    private fun startNotificationLoop() {
        coroutineScope.launch {
            while (true) {
                try {
                    val activeEvents = eventDao.getAllEvents()
                    Log.d("EventNotificationService", "Checking for active events")
                    for (event in activeEvents) {
                        Log.d("CheckingEvent", "startNotificationLoop: $event")
                        val (startTime, endTime) = event.eventTiming.toString().split(" - ")
                        if (isEventActive(startTime, endTime)) {
                            createNotification(event)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EventNotificationService", "Error checking events: ${e.message}")
                }
                delay(1 * 60 * 1000) // Check every 1 minutes
            }
        }
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
