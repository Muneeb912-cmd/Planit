package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.add_edit_event

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.eventmanagement.databinding.FragmentAddEditEventBinding
import com.example.eventmanagement.di.Categories
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.example.eventmanagement.utils.Response
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AddEditEventFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAddEditEventBinding
    private lateinit var eventLongitude: String
    private lateinit var eventLatitude: String

    @Inject
    @Categories
    lateinit var categories: ArrayList<String>
    private val viewModel: AddEditEventViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateDropDown()
        updateEventInfo()
        initializeListeners()
        validateFields()
    }

    private fun validateFields() {
        lifecycleScope.launch {
            viewModel.errors.collect { errorMap ->
                binding.eventTitle.error = errorMap["eventTitle"]
                binding.eventCategory.error = errorMap["eventCategory"]
                binding.eventEndTime.error = errorMap["eventEndTime"]
                binding.eventStartTime.error = errorMap["eventStartTime"]
                binding.eventEndTime.error = errorMap["eventEndTime"]
                binding.eventLocation.error = errorMap["eventLocation"]
                binding.eventDescription.error = errorMap["eventDescription"]
            }
        }
    }

    private fun updateEventInfo() {
        val currentUser = sharedViewModel.currentUser.value
        if (currentUser != null) {
            viewModel.updateEventInfo("eventOrganizer", currentUser.userName.toString())
            viewModel.updateEventInfo("eventCreatedBy", currentUser.userId.toString())
            viewModel.updateEventInfo("numberOfPeopleAttending", "0")
            viewModel.updateEventInfo("isEventPopular", "no")
        }
    }

    private fun populateDropDown() {
        val adapter =
            ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categories)
        binding.eventCategory.setAdapter(adapter)
        binding.eventCategory.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategory = parent.getItemAtPosition(position).toString()
            viewModel.updateEventInfo("eventCategory", selectedCategory)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeListeners() {
        with(binding) {
            eventTitle.addTextChangedListener {
                viewModel.updateEventInfo("eventTitle", it.toString())
            }
            eventDate.setOnClickListener {
                showDatePicker(eventDate, "eventDate")
            }
            rangePickerCheckBox.setOnClickListener {
                toggleEndDateVisibility()
            }
            eventEndDate.setOnClickListener {
                showDatePicker(eventEndDate, "eventEndDate")
            }
            eventStartTime.setOnClickListener {
                showTimePicker(eventStartTime, "startTime")
            }
            eventEndTime.setOnClickListener {
                showTimePicker(eventEndTime, "endTime")
            }
            eventLocation.setOnClickListener {
                val intent = Intent(requireContext(), LocationSearchActivity::class.java)
                locationSearchLauncher.launch(intent)
            }
            saveEventBtn.setOnClickListener {
                if (viewModel.isDataComplete) {
                    if (viewModel.isDataValid) {
                        observeSaveEventState()
                    } else {
                        showGenericDialog(
                            message = "Error: Couldn't validate the input data, please check and try again!",
                            title = "Input Data Not Valid",
                            iconResId = com.example.eventmanagement.R.drawable.ic_attention
                        )
                    }
                } else {
                    showGenericDialog(
                        message = "One or more input fields are empty. Kindly fill in the information before saving.",
                        title = "Field Empty",
                        iconResId = com.example.eventmanagement.R.drawable.ic_attention
                    )

                }
            }
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
            cancelBtn.setOnClickListener {
                dismiss()
            }
            eventFeaturedToggle.setOnClickListener {
                if (eventFeaturedToggle.isChecked) {
                    viewModel.updateEventInfo("isEventFeatured", "yes")
                } else {
                    viewModel.updateEventInfo("isEventFeatured", "no")
                }
            }
            visibilityToggle.setOnClickListener {
                if (visibilityToggle.isChecked) {
                    viewModel.updateEventInfo("isEventPublic", "yes")
                } else {
                    viewModel.updateEventInfo("isEventPublic", "no")
                }
            }

            eventDescription.addTextChangedListener {
                viewModel.updateEventInfo("eventDescription", it.toString())
            }
        }
    }


    private fun toggleEndDateVisibility() {
        if (binding.rangePickerCheckBox.isChecked) {
            binding.eventEndDateLayout.visibility = View.VISIBLE
        } else {
            viewModel.updateEventInfo("eventDate", binding.eventEndDate.text.toString())
            binding.eventEndDateLayout.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showDatePicker(editText: EditText, updateKey: String) {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)
                val formattedDate = "$year-$formattedMonth-$formattedDay"
                editText.setText(formattedDate)

                if (updateKey == "eventDate") {
                    viewModel.updateEventInfo("eventDate", formattedDate)
                } else if (updateKey == "eventEndDate") {
                    val startDate = binding.eventDate.text.toString()
                    viewModel.updateEventInfo("eventDate", "$startDate - $formattedDate")
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        when (updateKey) {
            "eventDate" -> {
                datePickerDialog.datePicker.minDate = calendar.timeInMillis
            }

            "eventEndDate" -> {
                val startDate = binding.eventDate.text.toString()
                if (startDate.isNotEmpty()) {
                    val startDateCalendar = Calendar.getInstance().apply {
                        val dateParts = startDate.split("-")
                        set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
                    }
                    startDateCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    datePickerDialog.datePicker.minDate = startDateCalendar.timeInMillis
                }
            }
        }

        datePickerDialog.show()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showTimePicker(editText: EditText, updateKey: String) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val formattedTime = if (is24HourFormat()) {
                    String.format("%02d:%02d", hourOfDay, minute)
                } else {
                    val hour =
                        if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                    String.format("%02d:%02d %s", hour, minute, amPm)
                }
                editText.setText(formattedTime)
                if (updateKey == "endTime") {
                    viewModel.updateEventInfo(
                        "eventTiming",
                        "${binding.eventStartTime.text.toString()} - ${binding.eventEndTime.text.toString()}"
                    )
                    viewModel.validateField(
                        "eventEndTime",
                        "${binding.eventStartTime.text.toString()} - ${binding.eventEndTime.text.toString()}"
                    )
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            is24HourFormat()
        )
        timePickerDialog.show()
    }

    private fun is24HourFormat(): Boolean {
        return android.text.format.DateFormat.is24HourFormat(requireContext())
    }

    private val locationSearchLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val locationName = result.data?.getStringExtra("location_name")
                val latitude = result.data?.getDoubleExtra("latitude", 0.0)
                val longitude = result.data?.getDoubleExtra("longitude", 0.0)
                binding.eventLocation.setText(locationName)
                eventLatitude = latitude.toString()
                eventLongitude = longitude.toString()
                viewModel.updateEventInfo("eventLocation", locationName ?: "")
                viewModel.updateEventInfo("eventLong", latitude.toString())
                viewModel.updateEventInfo("eventLat", longitude.toString())
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeSaveEventState() {
        viewModel.saveEvent()
        lifecycleScope.launch {
            viewModel.states.collect { result ->
                when (result) {
                    is Response.Error -> {
                        showGenericDialog(
                            message = "Error: Couldn't save event, please try again later",
                            title = "Event not saved",
                            iconResId = com.example.eventmanagement.R.drawable.ic_attention
                        )
                    }

                    Response.Loading -> binding.loaderOverlay.loaderOverlay.visibility =
                        View.VISIBLE

                    is Response.Success -> {
                        binding.loaderOverlay.loaderOverlay.visibility = View.GONE
                        Toast.makeText(requireContext(), "Event Added!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }
            }
        }
    }

    private fun showGenericDialog(message: String, title: String, iconResId: Int) {
        binding.loaderOverlay.loaderOverlay.visibility = View.GONE
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setMessage(message)
            .setTitle(title)
            .setIcon(iconResId)
            .setCancelable(false)
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.cancel()
            }
        val alertDialog = builder.create()
        alertDialog.show()
    }

}
