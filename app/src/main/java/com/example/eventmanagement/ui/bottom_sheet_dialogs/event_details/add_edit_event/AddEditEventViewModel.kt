package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.add_edit_event

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.OperationType
import com.example.eventmanagement.models.PendingOperations
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import com.example.eventmanagement.repository.room_db.Converters
import com.example.eventmanagement.repository.room_db.PendingOperationDao
import com.example.eventmanagement.receivers.ConnectivityObserver
import com.example.eventmanagement.utils.Response
import com.example.eventmanagement.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    private val validators: Validators,
    private val connectivityObserver: ConnectivityObserver,
    private val pendingOperationDao: PendingOperationDao,
    private val converters: Converters
) : ViewModel() {

    private val _events = MutableStateFlow(EventData())
    val eventsData: StateFlow<EventData> = _events.asStateFlow()


    private val _states = MutableStateFlow<Response<Unit>>(Response.Loading)
    val states: StateFlow<Response<Unit>> get() = _states.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String?>>(emptyMap())
    val errors: StateFlow<Map<String, String?>> get() = _errors


    var isDataComplete: Boolean = false
    var isDataValid: Boolean = false

    fun updateEventInfo(key: String, value: String) {
        val currentEvent = _events.value
        val updatedEvent = currentEvent.copy(
            eventId = if (key == "eventId") value else currentEvent.eventId,
            eventTitle = if (key == "eventTitle") value else currentEvent.eventTitle,
            eventOrganizer = if (key == "eventOrganizer") value else currentEvent.eventOrganizer,
            eventTiming = if (key == "eventTiming") value else currentEvent.eventTiming,
            eventCategory = if (key == "eventCategory") value else currentEvent.eventCategory,
            eventDescription = if (key == "eventDescription") value else currentEvent.eventDescription,
            eventLocation = if (key == "eventLocation") value else currentEvent.eventLocation,
            eventDate = if (key == "eventDate") value else currentEvent.eventDate,
            isEventFeatured = if (key == "isEventFeatured") value == "yes" else currentEvent.isEventFeatured,
            isEventPopular = if (key == "isEventPopular") value == "yes" else currentEvent.isEventPopular,
            numberOfPeopleAttending = if (key == "numberOfPeopleAttending") value.toIntOrNull() else currentEvent.numberOfPeopleAttending,
            isEventPublic = if (key == "isEventPublic") value == "yes" else currentEvent.isEventPublic,
            eventStatus = if (key == "eventStatus") value else currentEvent.eventStatus,
            eventCreatedBy = if (key == "eventCreatedBy") value else currentEvent.eventCreatedBy,
            eventLong = if (key == "eventLong") value else currentEvent.eventLong,
            eventLat = if (key == "eventLat") value else currentEvent.eventLat,
            isEventDeleted = if (key == "isEventDeleted") value == "Yes" else currentEvent.isEventDeleted,
        )
        validateField(key, value)
        checkIfDataComplete()
        _events.value = updatedEvent
    }

    private fun checkIfDataComplete() {
        isDataComplete = !eventsData.value.eventTitle.isNullOrEmpty() &&
                !eventsData.value.eventOrganizer.isNullOrEmpty() &&
                !eventsData.value.eventTiming.isNullOrEmpty() &&
                !eventsData.value.eventCategory.isNullOrEmpty() &&
                !eventsData.value.eventDescription.isNullOrEmpty() &&
                !eventsData.value.eventLocation.isNullOrEmpty() &&
                !eventsData.value.eventDate.isNullOrEmpty() &&
                eventsData.value.numberOfPeopleAttending != null &&
                eventsData.value.isEventFeatured != null &&
                eventsData.value.isEventPopular != null &&
                eventsData.value.isEventPublic != null &&
                !eventsData.value.eventCreatedBy.isNullOrEmpty()
    }


    private fun validateField(field: String, value: String) {
        val updatedErrors = _errors.value.toMutableMap()

        when (field) {
            "eventTitle" -> {
                updatedErrors["eventTitle"] =
                    if (validators.validateName(value)) null
                    else "Invalid Title. Example Input: (Promotion Ceremony)"
            }

            "eventCategory" -> updatedErrors["eventCategory"] =
                if (value.isNotBlank()) null else "Category cannot be empty."

            "eventDescription" -> updatedErrors["eventDescription"] =
                if (value.isNotBlank()) null else "Description cannot be empty."

            "eventLocation" -> updatedErrors["eventLocation"] =
                if (value.isNotBlank()) null else "Location cannot be empty."

            "eventDate" -> updatedErrors["eventDate"] =
                if (value.isNotBlank()) null else "Date cannot be empty."

        }
        isDataValid = updatedErrors.values.all { it == null }
        _errors.value = updatedErrors
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateEventStatus() {
        val currentDateTime = LocalDateTime.now()

        val eventDateString = eventsData.value.eventDate
        val eventTiming = eventsData.value.eventTiming

        var eventStartDateTime: LocalDateTime
        var eventEndDateTime: LocalDateTime

        if (eventDateString != null) {
            val dateRange = eventDateString.split(" - ")

            if (dateRange.size == 2) {
                val startDate =
                    LocalDate.parse(dateRange[0].trim(), DateTimeFormatter.ISO_LOCAL_DATE)
                val endDate = LocalDate.parse(dateRange[1].trim(), DateTimeFormatter.ISO_LOCAL_DATE)
                val startTime = LocalTime.MIN
                val endTime = LocalTime.MAX

                eventStartDateTime = startDate.atTime(startTime)
                eventEndDateTime = endDate.atTime(endTime)
            } else {
                // Event is a single date
                val eventDate = LocalDate.parse(eventDateString, DateTimeFormatter.ISO_LOCAL_DATE)
                eventStartDateTime = eventDate.atStartOfDay()
                eventEndDateTime = eventDate.atTime(LocalTime.MAX)
            }

            if (eventTiming != null && eventTiming.contains(" - ")) {
                // Handle time range if provided
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
                val times = eventTiming.split(" - ")
                if (times.size == 2) {
                    val startTime = LocalTime.parse(times[0].trim(), timeFormatter)
                    val endTime = LocalTime.parse(times[1].trim(), timeFormatter)
                    eventStartDateTime = eventStartDateTime.with(startTime)
                    eventEndDateTime = eventEndDateTime.with(endTime)
                }
            }

            val status = when {
                currentDateTime.toLocalDate() != eventStartDateTime.toLocalDate() && currentDateTime.isBefore(
                    eventStartDateTime
                ) -> "Up-Coming"

                currentDateTime.isAfter(eventEndDateTime) -> "Missed"
                else -> "On-Going"
            }

            _events.value = eventsData.value.copy(eventStatus = status)
        } else {
            _events.value = eventsData.value.copy(eventStatus = "Unknown")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveEvent() {
        updateEventStatus()
        updateEventInfo("isEventDeleted","No")
        _states.value = Response.Loading
        saveEventAsPendingOperation(eventsData.value,"ADD")
        _states.value = Response.Success(Unit)
    }

    private fun saveEventAsPendingOperation(eventData: EventData?, operation: String) {
        if (eventData != null) {
            val jsonData = converters.fromEvent(eventData)
            val operationType = when(operation) {
                "ADD" -> OperationType.ADD
                "UPDATE" -> OperationType.UPDATE
                "DELETE" -> OperationType.DELETE
                else -> throw IllegalArgumentException("Invalid operation type")
            }
            val pendingOperation = PendingOperations(
                operationType = operationType,
                documentId = eventData.eventId.toString(),
                data = jsonData,
                userId = eventData.eventCreatedBy.toString(),
                eventId = eventData.eventId.toString(),
                dataType = "event"
            )
            CoroutineScope(Dispatchers.IO).launch {
                pendingOperationDao.insert(pendingOperation)
            }
        } else {
            _states.value = Response.Error(Exception("Event data is null"))
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateEvent() {
        updateEventStatus()
        updateEventInfo("isEventDeleted","No")
        _states.value = Response.Loading
        saveEventAsPendingOperation(eventsData.value,"UPDATE")
        _states.value = Response.Success(Unit)
    }

    fun validateEventTiming(
        value: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        val timePattern = " - "
        val times = value.split(timePattern)

        if (times.size == 2) {
            val startTime = times[0].trim()
            val endTime = times[1].trim()

            when {
                startTime.isEmpty() && endTime.isEmpty() -> {
                    callback(
                        false,
                        "Event start time not selected. Event end time not selected.",
                        "both"
                    )
                }

                startTime.isEmpty() -> {
                    callback(false, "Event start time not selected.", "start")
                }

                endTime.isEmpty() -> {
                    callback(false, "Event end time not selected.", "end")
                }

                else -> {
                    val isStartTimeValid = validators.validateEventStartTime(startTime, endTime)
                    val isEndTimeValid = validators.validateEventEndTimings(startTime, endTime)

                    when {
                        !isEndTimeValid -> {
                            callback(
                                false,
                                "Invalid end time. Ensure it's after the start time and the minimum event time is 15 minutes.",
                                "end"
                            )
                        }

                        !isStartTimeValid -> {
                            callback(
                                false,
                                "Invalid start time. Ensure it's before the end time and the minimum event time is 15 minutes.",
                                "start"
                            )
                        }


                        else -> {
                            callback(true, "Event timing is valid.", "none")
                        }
                    }
                }
            }
        } else {
            callback(false, "Timing cannot be empty or incomplete.", "both")
        }
    }

}