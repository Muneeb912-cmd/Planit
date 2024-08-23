package com.example.eventmanagement.utils

interface Validators {
    fun validateName(name:String):Boolean
    fun validateEmail(email:String):Boolean
    fun validatePhone(phone:String):Boolean
    fun validatePassword(password:String):Boolean
    fun validateEventEndTimings(eventStartTime:String,eventEndTime:String):Boolean

}