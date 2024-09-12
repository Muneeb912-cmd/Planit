package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentEditProfileBinding
import com.example.eventmanagement.models.User
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.example.eventmanagement.utils.Response
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class EditProfileFragment(
    private val key: String,
    private val userData: User.UserData?
) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private val viewModel: EditProfileViewModel by viewModels()
    private lateinit var userImg: String
    private var onDismissListener: (() -> Unit)? = null
    private var currentEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (key == "Modify") {
            populateUserDataFromParams()
            binding.banUserBtn.visibility = View.VISIBLE
            binding.editUserTitle.text = "Modify User"
            binding.editUserTv.text = "Edit User Information"

        } else {
            binding.banUserBtn.visibility = View.GONE
            binding.editUserTitle.text = "Edit Profile"
            binding.editUserTv.text = "Update your personal information"
            populateCurrentUserData()
        }
        initializeListeners()
        userImg=userData?.userImg.toString()
    }


    private fun initializeListeners() {
        initializeImagePicker()
        with(binding) {
            addImage.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }
            userDob.setOnClickListener {
                showDatePicker()
            }
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
            updateUserBtn.setOnClickListener {
                updateUserData()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUserData() {
        val userName = binding.fullName.text.toString()
        val userPhone = binding.userPhone.text.toString()
        val userDob = binding.userDob.text.toString()
        val userId = if (key == "Modify") {
            userData?.userId
        } else {
            sharedViewModel.currentUser.value?.userId.toString()
        }

        viewModel.updateUserData(
            userId.toString(),
            userName,
            userPhone,
            userDob,
            userImg
        )

        lifecycleScope.launch {
            viewModel.editDataStates.collect { response ->
                when (response) {
                    is Response.Success -> {

                        binding.msgTv.visibility = View.VISIBLE
                        binding.msgTv.text = "User Data successfully updated!"
                        binding.updateUserBtn.text = "Update"
                        binding.updateUserBtn.isClickable = true
                        dismiss()

                    }

                    is Response.Error -> {
                        binding.msgTv.visibility = View.VISIBLE
                        binding.msgTv.text =
                            "Couldn't Update User Data, check internet and try again!"
                        binding.updateUserBtn.isClickable = true
                    }

                    is Response.Loading -> {
                        binding.updateUserBtn.text = "Updating User: Please Wait..."
                        binding.updateUserBtn.isClickable = false
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateUserDataFromParams() {
        with(binding) {
            fullName.setText(userData?.userName)
            userPhone.setText(userData?.userPhone)
            userDob.setText(userData?.userDob)
            Glide.with(requireContext())
                .load(userData?.userImg)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                )
                .into(addImage)

            if (userData?.userBanned == true) {
                banUserBtn.text = "Un-Suspend User"
            } else {
                banUserBtn.text = "Suspend User"
            }

            banUserBtn.setOnClickListener {
                toggleUserBanStatus()
            }
        }
    }

    private fun toggleUserBanStatus() {
        val newBanStatus = !(userData?.userBanned ?: false)
        val newButtonText = if (newBanStatus) "Un-Suspend User" else "Suspend User"
        viewModel.updateUserBanStatus(userData?.userId ?: "", newBanStatus) { result ->
            if (result) {
                Toast.makeText(
                    requireContext(),
                    "User Account Suspend Status Updated!",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error Suspending User Account",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.banUserBtn.text = newButtonText
    }

    private fun populateCurrentUserData() {
        with(binding) {
            fullName.setText(sharedViewModel.currentUser.value?.userName)
            currentEmail = sharedViewModel.currentUser.value?.userEmail.toString()
            userPhone.setText(sharedViewModel.currentUser.value?.userPhone)
            userDob.setText(sharedViewModel.currentUser.value?.userDob)
            Glide.with(requireContext())
                .load(sharedViewModel.currentUser.value?.userImg)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                )
                .into(addImage)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.userDob.setText("$day-${month + 1}-$year")
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
                    userImg = imageUri.toString()
                }
            }
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
}