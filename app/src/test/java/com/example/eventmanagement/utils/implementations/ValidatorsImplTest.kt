package com.example.eventmanagement.utils.implementations

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.time.format.DateTimeParseException

class ValidatorsImplTest {

    private val validators = ValidatorsImpl()

    @Test
    fun validateName_ValidName_ReturnsTrue() {
        assertTrue(validators.validateName("John Doe"))
    }

    @Test
    fun validateName_EmptyName_ReturnsFalse() {
        assertFalse(validators.validateName(""))
    }

    @Test
    fun validateName_InvalidName_ReturnsFalse() {
        assertFalse(validators.validateName("John123"))
    }

    @Test
    fun validateEmail_ValidEmail_ReturnsTrue() {
        assertTrue(validators.validateEmail("test@example.com"))
    }

    @Test
    fun validateEmail_InvalidEmail_ReturnsFalse() {
        assertFalse(validators.validateEmail("invalid-email"))
    }

    @Test
    fun validatePhone_ValidPhone_ReturnsTrue() {
        assertTrue(validators.validatePhone("+12345678901"))
    }

    @Test
    fun validatePhone_InvalidPhone_ReturnsFalse() {
        assertFalse(validators.validatePhone("1234"))
    }

    @Test
    fun validatePassword_ValidPassword_ReturnsTrue() {
        assertTrue(validators.validatePassword("Password1!"))
    }

    @Test
    fun validatePassword_InvalidPassword_ReturnsFalse() {
        assertFalse(validators.validatePassword("pass"))
        assertFalse(validators.validatePassword("PASSWORD"))
        assertFalse(validators.validatePassword("123456"))
    }

    @Test
    fun validateEventEndTimings_ValidTimes_ReturnsTrue() {
        assertTrue(validators.validateEventEndTimings("09:00 AM", "10:00 AM"))
    }

    @Test
    fun validateEventEndTimings_EndTimeBeforeStartTime_ReturnsFalse() {
        assertFalse(validators.validateEventEndTimings("11:00 AM", "10:00 AM"))
    }

    @Test
    fun validateEventEndTimings_LessThan15MinutesDuration_ReturnsFalse() {
        assertFalse(validators.validateEventEndTimings("10:00 AM", "10:14 AM"))
    }

    @Test
    fun validateEventEndTimings_InvalidTimeFormat_ReturnsFalse() {
        assertFalse(validators.validateEventEndTimings("10:00 AM", "invalid-time"))
    }

    @Test
    fun validateEventStartTime_ValidTimes_ReturnsTrue() {
        assertTrue(validators.validateEventStartTime("10:00 AM", "12:30 PM"))
    }

    @Test
    fun validateEventStartTime_StartTimeAfterEndTime_ReturnsFalse() {
        assertFalse(validators.validateEventStartTime("11:00 AM", "10:00 AM"))
    }

    @Test
    fun validateEventStartTime_LessThan15MinutesDuration_ReturnsFalse() {
        assertFalse(validators.validateEventStartTime("10:00 AM", "10:14 AM"))
    }

    @Test
    fun validateEventStartTime_InvalidTimeFormat_ReturnsFalse() {
        assertFalse(validators.validateEventStartTime("10:00 AM", "invalid-time"))
    }
}
