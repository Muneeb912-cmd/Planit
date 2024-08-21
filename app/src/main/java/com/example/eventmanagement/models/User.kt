package com.example.eventmanagement.models

sealed class User {
    data class UserData(
        var userId: String? = null,
        var userName: String? = null,
        var userEmail: String? = null,
        var userPhone: String? = null,
        var userDob: String? = null,
        var userPassword: String? = null,
        var userRole: String? = null,
        var userImg: String? = null,
        var userLocation:String?=null,
        var userLoginType: String? = null,
        var isNotificationsAllowed:Boolean?=null,
        var isProfilePrivate:Boolean?=null
    )
    data class AttendingEvents(
        var userId: String? = null,
        var eventId:String?=null
    )
    data class FavEvents(
        var userId: String? = null,
        var eventId:String?=null
    )
    data class CreatedEvents(
        var userId: String? = null,
        var eventId:String?=null
    )
}