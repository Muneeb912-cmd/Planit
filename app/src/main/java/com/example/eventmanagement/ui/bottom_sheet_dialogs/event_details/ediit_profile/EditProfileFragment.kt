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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class EditProfileFragment : BottomSheetDialogFragment() {
    private lateinit var binding:FragmentEditProfileBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeImagePicker()
        binding.addImage.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Launch image picker
        }
        binding.userDob.setOnClickListener {
            showDatePicker()
        }
        binding.closeBottomSheet.setOnClickListener {
            dismiss()
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
                }
            }
    }
}