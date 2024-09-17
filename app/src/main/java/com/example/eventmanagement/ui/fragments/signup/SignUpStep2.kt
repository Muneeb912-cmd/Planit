package com.example.eventmanagement.ui.fragments.signup

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentSignUpStep2Binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class SignUpStep2 : Fragment() {

    private lateinit var binding: FragmentSignUpStep2Binding
    private val signUpViewModel: SignUpViewModel by activityViewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var loginType: String
    private val countryCode = "+92 "

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpStep2Binding.inflate(inflater, container, false)
        loginType = signUpViewModel.loginType
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (signUpViewModel.loginType == "google") {
            binding.passwordLayout.visibility = View.GONE
            binding.confirmPasswordLayout.visibility = View.GONE
            loadCurrentUserData()
        }

        setupListeners()
        initializeImagePicker()
        observeButtonClick()
        binding.addImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        setupPhoneInput()
    }

    private fun observeButtonClick() {
        lifecycleScope.launch {
            signUpViewModel.clearFocusEvent.collect { clicked ->
                if (clicked) {
                    clearInputFieldFocus()
                }
            }
        }
    }

    private fun clearInputFieldFocus() {
        with(binding) {
            fullName.clearFocus()
            userEmail.clearFocus()
            userPhone.clearFocus()
            userDob.clearFocus()
            password.clearFocus()
            confirmPassword.clearFocus()
            signUpViewModel.unTriggerClearFocus()
        }
    }

    private fun setupPhoneInput() {
        binding.userPhone.setText(countryCode)
        binding.userPhone.addTextChangedListener {
            val currentText = it.toString()
            if (!currentText.startsWith(countryCode)) {
                binding.userPhone.setText(countryCode)
                binding.userPhone.setSelection(binding.userPhone.text?.length ?: 0)
            }
        }
    }

    private fun loadCurrentUserData() {
        val currentUser = signUpViewModel.getCurrentUser()
        currentUser.let {
            binding.fullName.setText(it?.userName)
            binding.userEmail.setText(it?.userEmail)
            binding.userPhone.setText(it?.userPhone)
            Glide.with(this)
                .load(it?.userImg)
                .apply(RequestOptions().placeholder(R.drawable.ic_placeholder))
                .into(binding.addImage)
            signUpViewModel.updateUserInfo("fullName", it?.userName.toString())
            signUpViewModel.updateUserInfo("email", it?.userEmail.toString())
            signUpViewModel.updateUserInfo("phone", it?.userPhone.toString())
            signUpViewModel.updateUserInfo("img", it?.userImg.toString())
            signUpViewModel.updateUserInfo("userId", it?.userId.toString())

        }
    }

    private fun setupListeners() {
        binding.fullName.addTextChangedListener {
            val error = signUpViewModel.validateFullName(binding.fullName.text.toString())
            binding.fullNameLayout.error = error
            signUpViewModel.updateUserInfo("fullName", it.toString())
        }

        binding.userEmail.addTextChangedListener {
            binding.emailLayout.error = null
            signUpViewModel.updateUserInfo("email", it.toString())
        }

        binding.userEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val error = signUpViewModel.validateEmail(binding.userEmail.text.toString())
                binding.emailLayout.error = error
            }
        }

        binding.userPhone.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val phone = binding.userPhone.text.toString()
                val phnStr = phone.replace("\\s".toRegex(), "")
                if (phnStr.isEmpty()) {
                    binding.phoneLayout.error = null
                } else if (phnStr.count() == 13) {
                    binding.phoneLayout.error = null
                    signUpViewModel.updateUserInfo("phone", phnStr)
                } else {
                    binding.phoneLayout.error = "Phone number should be 10 digits Long"
                }
            }
        }
        binding.userPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.phoneLayout.error = null
                signUpViewModel.updateUserInfo("phone", s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (!input.matches("^\\+92\\s?[0-9]*$".toRegex())) {
                    binding.phoneLayout.error = "Only digits allowed after country code"
                }
                if (input.contains("\\+92\\s{2,}".toRegex())) {
                    binding.phoneLayout.error = "Only one space allowed after +92"
                }
            }
        })

        binding.password.addTextChangedListener {
            binding.passwordLayout.error = null
            if (!binding.confirmPassword.text.isNullOrEmpty()){
                val password = binding.password.text.toString()
                val confPass = binding.confirmPassword.text.toString()
                if (password == confPass) {
                    signUpViewModel.isDataValid = true
                } else {
                    signUpViewModel.isDataValid = false
                    binding.confirmPasswordLayout.error =
                        "Password and confirm password didn't match!"
                    binding.confirmPasswordLayout.errorIconDrawable = null
                }

            }
        }

        binding.password.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val error = signUpViewModel.validatePassword(binding.password.text.toString())
                if (error != null) {
                    binding.passwordLayout.error = error
                    binding.passwordLayout.errorIconDrawable = null
                }

            }
        }

        binding.confirmPassword.addTextChangedListener {
            binding.confirmPasswordLayout.error = null
        }

        binding.confirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = binding.password.text.toString()
                val confPass = binding.confirmPassword.text.toString()
                if (password == confPass) {
                    signUpViewModel.isDataValid = true
                    signUpViewModel.updateUserInfo("password", confPass)
                } else {
                    signUpViewModel.isDataValid = false
                    binding.confirmPasswordLayout.error =
                        "Password and confirm password didn't match!"
                    binding.confirmPasswordLayout.errorIconDrawable = null
                }
            }
        }
        binding.userDob.setOnClickListener {
            showDatePicker()
            binding.userPhone.clearFocus()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.userDob.setText("$day-${month + 1}-$year")
                signUpViewModel.updateUserInfo("dob", "$day-${month + 1}-$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val minDateCalendar = Calendar.getInstance().apply {
            set(1950, Calendar.JANUARY, 1)
        }
        datePickerDialog.datePicker.minDate = minDateCalendar.timeInMillis
        val maxDateCalendar = Calendar.getInstance().apply {
            set(2020, Calendar.DECEMBER, 31)
        }
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis
        datePickerDialog.show()
    }

    private fun initializeImagePicker() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let { imageUri ->
                    Glide.with(requireContext())
                        .load(imageUri)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.ic_placeholder)
                                .error(R.drawable.ic_placeholder)
                        )
                        .into(binding.addImage)
                    signUpViewModel.updateUserInfo("img", imageUri.toString())
                }
            }
    }

}
