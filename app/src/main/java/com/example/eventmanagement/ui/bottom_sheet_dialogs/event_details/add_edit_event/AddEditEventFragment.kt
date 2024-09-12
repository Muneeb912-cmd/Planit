package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.add_edit_event

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
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
import com.example.eventmanagement.models.EventData
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
    private var onDismissListener: (() -> Unit)? = null

    @Inject
    @Categories
    lateinit var categories: ArrayList<String>
    private val viewModel: AddEditEventViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var key: String? = null
    private var eventData: EventData? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddEditEventBinding.inflate(inflater, container, false)

        arguments?.let {
            key = it.getString("key")
            eventData = it.getSerializable("eventData") as EventData?
        }

        // Populate data if updating
        if (key == "update") {
            populateEventData(eventData)
            binding.saveEventBtn.text = "Update"
            binding.addEditTitle.text="Update Event"
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateDropDown()
        if (key == "create") updateEventInfo()
        initializeListeners()
        validateFields()
    }


    private fun populateEventData(eventData: EventData?) {
        eventData?.let {
            binding.eventTitle.setText(it.eventTitle)
            binding.eventCategory.setText(it.eventCategory)
            binding.eventLocation.setText(it.eventLocation)
            binding.eventFeaturedToggle.isChecked = it.isEventFeatured == true
            binding.visibilityToggle.isChecked = it.isEventPublic == true
            binding.eventDescription.setText(it.eventDescription)
            binding.eventCategory.setText(it.eventCategory)

            // Handle date range
            if (!it.eventDate.isNullOrEmpty()) {
                val dates = it.eventDate!!.split(" - ")
                if (dates.size == 2) {
                    binding.eventDate.setText(dates[0])
                    binding.eventEndDate.setText(dates[1])
                    binding.rangePickerCheckBox.isChecked = true
                    binding.eventEndDateLayout.visibility = View.VISIBLE
                } else {
                    binding.eventDate.setText(it.eventDate)
                    binding.eventEndDate.setText("")
                    binding.rangePickerCheckBox.isChecked = false
                    binding.eventEndDateLayout.visibility = View.GONE
                }
            }

            if (!it.eventTiming.isNullOrEmpty()) {
                val startTime = it.eventTiming!!.split(" - ")
                if (startTime.size == 2) {
                    binding.eventStartTime.setText(startTime[0])
                    binding.eventEndTime.setText(startTime[1])
                } else {
                    binding.eventStartTime.setText(it.eventTiming)
                    binding.eventEndTime.setText("")
                }
            }
            updateDataInViewModel(it)
        }
    }

    private fun updateDataInViewModel(it: EventData) {
        // Update ViewModel with existing event data
        viewModel.updateEventInfo("eventId", it.eventId ?: "")
        viewModel.updateEventInfo("eventTitle", it.eventTitle ?: "")
        viewModel.updateEventInfo("eventOrganizer", it.eventOrganizer ?: "")
        viewModel.updateEventInfo("eventTiming", it.eventTiming ?: "")
        viewModel.updateEventInfo("eventCategory", it.eventCategory ?: "")
        viewModel.updateEventInfo("eventDescription", it.eventDescription ?: "")
        viewModel.updateEventInfo("eventLocation", it.eventLocation ?: "")
        viewModel.updateEventInfo("eventDate", it.eventDate ?: "")
        viewModel.updateEventInfo(
            "isEventFeatured",
            if (it.isEventFeatured == true) "yes" else "no"
        )
        viewModel.updateEventInfo("isEventPopular", if (it.isEventPopular == true) "yes" else "no")
        viewModel.updateEventInfo(
            "numberOfPeopleAttending",
            it.numberOfPeopleAttending?.toString() ?: ""
        )
        viewModel.updateEventInfo("isEventPublic", if (it.isEventPublic == true) "yes" else "no")
        viewModel.updateEventInfo("eventStatus", it.eventStatus ?: "")
        viewModel.updateEventInfo("eventCreatedBy", it.eventCreatedBy ?: "")
        viewModel.updateEventInfo("eventLong", it.eventLong ?: "")
        viewModel.updateEventInfo("eventLat", it.eventLat ?: "")
        viewModel.updateEventInfo("isEventDeleted", if (it.isEventDeleted == true) "yes" else "no")
    }


    private fun validateFields() {
        lifecycleScope.launch {
            viewModel.errors.collect { errorMap ->
                binding.eventTitle.error = errorMap["eventTitle"]
                binding.eventCategory.error = errorMap["eventCategory"]
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
        binding.saveEventBtn.setOnClickListener {
            if (key == "create") {
                viewModel.saveEvent()
            } else if (key == "update") {
                viewModel.saveEvent()
            }
        }
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
                }
                viewModel.validateEventTiming(
                    "${binding.eventStartTime.text.toString()} - ${binding.eventEndTime.text.toString()}"
                ) { result, msg, fieldType ->
                    if (result) {
                        binding.eventStartTime.error = null
                        binding.eventEndTime.error = null
                        binding.timeError.visibility = View.GONE
                    } else {
                        binding.timeError.visibility = View.VISIBLE
                        binding.timeError.text = msg
                        when (fieldType) {
                            "start" -> binding.eventStartTime.error = msg
                            "end" -> binding.eventEndTime.error = msg
                            "both" -> {
                                binding.eventStartTime.error = msg
                                binding.eventEndTime.error = msg
                            }
                        }
                    }
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
        if (key == "create") {
            viewModel.saveEvent()
        } else {
            viewModel.updateEvent()
        }
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
                        Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
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

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

}
