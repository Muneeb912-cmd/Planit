package com.example.eventmanagement.repository.firebase.user_data

import com.example.eventmanagement.models.User
import com.example.eventmanagement.utils.PreferencesUtil
import com.google.common.base.Joiner.on
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UserDataImplTest {


    @Test
    fun getAllUserData() = runTest {
    }



    @Test
    fun getCurrentUser() {
    }

    @Test
    fun getUserDataFromFireStore() {
    }

    @Test
    fun updateUserLocation() {
    }

    @Test
    fun updateUserNotificationSettings() {
    }

    @Test
    fun updateUserProfileStatus() {
    }

    @Test
    fun updateUserProfile() {
    }

    @Test
    fun removeCurrentUserListener() {
    }

    @Test
    fun updateUserBanStatus() {
    }
}