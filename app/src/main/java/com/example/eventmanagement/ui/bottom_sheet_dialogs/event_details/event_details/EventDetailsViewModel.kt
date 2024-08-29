package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val eventDataMethods: EventDataMethods
) :ViewModel() {

    private val _eventAttendee = MutableStateFlow<List<String>>(emptyList())
    val eventAttendee: StateFlow<List<String>> get() = _eventAttendee.asStateFlow()
    fun observeEventAttendees(eventId:String){
        viewModelScope.launch {
            eventDataMethods.observeAttendeesByEventId(eventId){result,attendees->
                if(result){
                    _eventAttendee.value=attendees
                }
            }
        }
    }
    fun addUserAsAttendee(eventId:String,userId:String,onResult: (Boolean)->Unit){
        viewModelScope.launch {
            eventDataMethods.addAttendeeUpdatePeopleGoingCount(eventId,userId){result->
                if(result){
                    onResult(true)
                }else{
                    onResult(false)
                }
            }
        }
    }
    fun removeUserAsAttendee(eventId:String,userId:String,onResult: (Boolean)->Unit){
        viewModelScope.launch {
            eventDataMethods.removeAttendeeUpdatePeopleGoingCount(eventId,userId){result->
                if(result){
                    onResult(true)
                }else{
                    onResult(false)
                }
            }
        }
    }
    fun clearEventAttendees() {
        _eventAttendee.value = emptyList()
    }
}