package com.example.eventmanagement.utils.implementations

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.eventmanagement.utils.Validators
import java.util.regex.Pattern
import javax.inject.Inject
import java.time.LocalTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ValidatorsImpl @Inject constructor(): Validators {

    override fun validateName(name: String): Boolean {
        return name.isNotBlank() && name.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    override fun validateEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    override fun validatePhone(phone: String): Boolean {
        val phonePattern = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
        )
        return phonePattern.matcher(phone).matches()
    }

    override fun validatePassword(password: String): Boolean {
        val hasSpecialChar = Pattern.compile("[!@#\$%^&*(),.?\":{}|<>]").matcher(password).find()
        val hasUppercase = Pattern.compile("[A-Z]").matcher(password).find()
        val hasNumber = Pattern.compile("[0-9]").matcher(password).find()
        val minLength = 5

        return password.length >= minLength && hasSpecialChar && hasUppercase && hasNumber
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun validateEventEndTimings(eventStartTime: String, eventEndTime: String): Boolean {
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        return try {
            val startTime = LocalTime.parse(eventStartTime, timeFormatter)
            val endTime = LocalTime.parse(eventEndTime, timeFormatter)
            val isAfter = endTime.isAfter(startTime)
            val duration = Duration.between(startTime, endTime).toMinutes()
            isAfter && duration >= 15
        } catch (e: DateTimeParseException) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun validateEventStartTime(eventStartTime: String, eventEndTime: String): Boolean {
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        return try {
            val startTime = LocalTime.parse(eventStartTime, timeFormatter)
            val endTime = LocalTime.parse(eventEndTime, timeFormatter)
            val isBefore = startTime.isBefore(endTime)
            val duration = Duration.between(startTime, endTime).toMinutes()
            isBefore && duration >= 15
        } catch (e: DateTimeParseException) {
            false
        }
    }


}
