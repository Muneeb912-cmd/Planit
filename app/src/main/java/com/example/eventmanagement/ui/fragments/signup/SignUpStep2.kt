package com.example.eventmanagement.ui.fragments.signup

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.launch
import java.util.Calendar

class SignUpStep2 : Fragment() {

    private lateinit var binding: FragmentSignUpStep2Binding
    private val signUpViewModel: SignUpViewModel by activityViewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var loginType:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpStep2Binding.inflate(inflater, container, false)
        loginType=signUpViewModel.loginType
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
        binding.addImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }



        lifecycleScope.launch {
            signUpViewModel.errors.collect { errorMap ->
                binding.fullNameLayout.error = errorMap["fullName"]
                binding.emailLayout.error = errorMap["email"]
                binding.phoneLayout.error = errorMap["phone"]
                binding.passwordLayout.error = errorMap["password"]
                binding.confirmPasswordLayout.error = errorMap["confirmPassword"]
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
            signUpViewModel.updateUserInfo("fullName",it?.userName.toString())
            signUpViewModel.updateUserInfo("email",it?.userEmail.toString())
            signUpViewModel.updateUserInfo("phone",it?.userPhone.toString())
            signUpViewModel.updateUserInfo("img",it?.userImg.toString())
            signUpViewModel.updateUserInfo("userId",it?.userId.toString())

        }
    }

    private fun setupListeners() {

        binding.userDob.setOnClickListener {
            showDatePicker()
        }

        binding.fullName.addTextChangedListener {
            signUpViewModel.updateUserInfo("fullName", it.toString())
        }

        binding.userEmail.addTextChangedListener {
            signUpViewModel.updateUserInfo("email", it.toString())
        }

        binding.userPhone.addTextChangedListener {
            signUpViewModel.updateUserInfo("phone", it.toString())
        }

        binding.password.addTextChangedListener {
            if (loginType != "google") {
                signUpViewModel.updateUserInfo("password", it.toString())
            }
        }

        binding.confirmPassword.addTextChangedListener {
            if (loginType != "google") {
                signUpViewModel.updateUserInfo("confirmPassword", it.toString())
            }
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
