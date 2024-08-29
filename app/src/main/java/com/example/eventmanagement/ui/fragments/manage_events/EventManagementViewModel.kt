package com.example.eventmanagement.ui.fragments.manage_events

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class EventManagementViewModel @Inject constructor(
    private val eventDataMethods: EventDataMethods
): ViewModel() {
    fun deleteEventById(eventId:String,deleteStatus:Boolean, onResult: (Boolean)->Unit){
        eventDataMethods.deleteEventById(
            eventId,deleteStatus
        ){result->
            if(result){
                onResult(true)
            }else{
                onResult(false)
            }
        }
    }
}