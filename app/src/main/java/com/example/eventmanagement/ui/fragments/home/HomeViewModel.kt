package com.example.eventmanagement.ui.fragments.home

import androidx.lifecycle.ViewModel
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.repository.firebase.events_data.EventDataMethods
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val eventDataMethods: EventDataMethods
): ViewModel() {
    fun addEventToUserFav(userId:String,eventData:EventData, onResult: (Boolean,String) -> Unit){
        eventDataMethods.addEventToUserFav(userId,eventData){result,msg->
            if (result){
                onResult(true,msg)
            }else{
                onResult(false,msg)
            }
        }
    }

    fun removeEventFromUserFav(userId:String,eventData:EventData, onResult: (Boolean,String) -> Unit){
        eventDataMethods.removeEventFromUserFav(userId,eventData){result,msg->
            if (result){
                onResult(true,msg)
            }else{
                onResult(false,msg)
            }
        }
    }
}