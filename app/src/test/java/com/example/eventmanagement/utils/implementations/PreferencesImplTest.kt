package com.example.eventmanagement.utils.implementations

import android.content.Context
import android.content.SharedPreferences
import com.example.eventmanagement.models.User
import com.google.gson.Gson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.junit.jupiter.api.Assertions.*

class PreferencesImplTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferencesUtil: PreferencesImpl
    private val gson = Gson()

    @BeforeEach
    fun setUp() {
        // Create mock SharedPreferences and Editor
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)

        // Configure the mock Editor to return itself when edit() is called
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), anyString())).thenReturn(editor)
        `when`(editor.remove(anyString())).thenReturn(editor)
        `when`(editor.apply()).then {}

        // Mock the Context and SharedPreferences
        val context = mock(Context::class.java)
        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)

        // Initialize PreferencesImpl with the mocked Context
        preferencesUtil = PreferencesImpl(context)
    }

    @Test
    fun saveUser_SavesUserToPreferences() {
        val user = User.UserData(0,"John Doe", "john.doe@example.com")
        val userJson = gson.toJson(user)

        preferencesUtil.saveUser(user)

        verify(editor).putString("user", userJson)
        verify(editor).apply()
    }

    @Test
    fun getUser_ReturnsUserFromPreferences() {
        val user = User.UserData(0,"John Doe", "john.doe@example.com")
        val userJson = gson.toJson(user)
        `when`(sharedPreferences.getString("user", null)).thenReturn(userJson)

        val result = preferencesUtil.getUser()

        assertNotNull(result)
        assertEquals(user, result)
    }

    @Test
    fun getUser_ReturnsNullIfNoUser() {
        `when`(sharedPreferences.getString("user", null)).thenReturn(null)

        val result = preferencesUtil.getUser()

        assertNull(result)
    }

    @Test
    fun updateUser_UpdatesUserInPreferences() {
        val user = User.UserData(0,"Jane Doe", "jane.doe@example.com")
        val userJson = gson.toJson(user)

        preferencesUtil.updateUser(user)

        verify(editor).putString("user", userJson)
        verify(editor).apply()
    }

    @Test
    fun deleteUser_RemovesUserFromPreferences() {
        preferencesUtil.deleteUser()

        verify(editor).remove("user")
        verify(editor).apply()
    }
}
