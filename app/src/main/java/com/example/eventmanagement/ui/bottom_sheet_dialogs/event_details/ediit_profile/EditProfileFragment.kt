package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentEditProfileBinding
import com.example.eventmanagement.ui.shared_view_model.UserDataViewModel
import com.example.eventmanagement.utils.Response
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : BottomSheetDialogFragment() {

    private lateinit var binding:FragmentEditProfileBinding
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private val viewModel:EditProfileViewModel by viewModels()
    private var userImg:String=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateData()
        initializeListeners()
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
            updateUserBtn.setOnClickListener{
                updateUserData()
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUserData() {
        val userName = binding.fullName.text.toString()
        val userEmail = binding.userEmail.text.toString()
        val userPhone = binding.userPhone.text.toString()
        val userDob = binding.userDob.text.toString()

        viewModel.updateUserData(
            userDataViewModel.currentUser.value?.userId.toString(),
            userName,
            userEmail,
            userPhone,
            userDob,
            userImg,
        )

        lifecycleScope.launch {
            viewModel.editDataStates.collect() { response ->
                when (response) {
                    is Response.Success -> {
                        if (userEmail != userDataViewModel.currentUser.value?.userEmail) {
                            showEmailVerificationDialog()
                        } else {
                            binding.msgTv.visibility=View.VISIBLE
                            binding.msgTv.text="User Data successfully updated!"
                            binding.updateUserBtn.isClickable=false
                        }
                    }
                    is Response.Error -> {
                        binding.msgTv.visibility=View.VISIBLE
                        binding.msgTv.text="Couldn't Update User Data, check internet and try again!"
                        binding.updateUserBtn.isClickable=true
                    }
                    is Response.Loading -> {
                        binding.msgTv.visibility=View.VISIBLE
                        binding.msgTv.text="Updating User: Please Wait..."
                        binding.updateUserBtn.isClickable=false
                    }
                }
            }
        }
    }

    private fun showEmailVerificationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Email Verification")
            .setMessage("Your email has been updated. Please verify your new email address by checking your inbox.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }



    private fun populateData() {
        with(binding) {
            fullName.setText(userDataViewModel.currentUser.value?.userName)
            userEmail.setText(userDataViewModel.currentUser.value?.userEmail)
            userPhone.setText(userDataViewModel.currentUser.value?.userPhone)
            userDob.setText(userDataViewModel.currentUser.value?.userDob)
            Glide.with(requireContext())
                .load(userDataViewModel.currentUser.value?.userImg)
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
                    userImg=imageUri.toString()
                }
            }
    }
}