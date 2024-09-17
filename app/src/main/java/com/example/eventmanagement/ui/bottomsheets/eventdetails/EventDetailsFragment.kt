package com.example.eventmanagement.ui.bottomsheets.eventdetails

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.eventmanagement.databinding.FragmentEventDetailsBinding
import com.example.eventmanagement.models.Attendees
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventDetailsFragment(private val cardData: EventData) : BottomSheetDialogFragment(),
    OnMapReadyCallback {

    private lateinit var binding: FragmentEventDetailsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: com.google.android.gms.maps.MapView
    private var eventLocation: LatLng? = null
    private var onDismissListener: (() -> Unit)? = null
    private val viewModel: EventDetailsViewModel by viewModels()
    private val sharedViewModel:SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        val latLng = cardData.eventLong + "," + cardData.eventLat
        eventLocation = parseLocation(latLng)
        observeEventAttendees()

        if(sharedViewModel.currentUser.value?.userRole!="Admin"){
            binding.attendingEventLayout.visibility=View.VISIBLE
        }else{
            binding.attendingEventLayout.visibility=View.GONE
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            eventTitle.text = cardData.eventTitle
            eventTime.text = cardData.eventTiming
            eventDate.text = cardData.eventDate
            eventOrganizer.text = "Organizer: ${cardData.eventOrganizer}"
            eventDescription.text = cardData.eventDescription
            eventCategory.text = "Category: ${cardData.eventCategory}"
            eventStatus.text = "Status: ${cardData.eventStatus}"
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
            if (cardData.eventStatus == "Missed") {
                attendingTv.text = "You missed the Event!"
                attendingToggleButton.visibility = View.GONE
            } else {
                attendingToggleButton.visibility = View.VISIBLE
                attendingToggleButton.setOnCheckedChangeListener(null)
                attendingToggleButton.isChecked = false
            }
            updateToggleButtonState(sharedViewModel.observeCurrentUserFromAttendees.value)
            binding.attendingToggleButton.setOnCheckedChangeListener { _, isChecked ->
                handleToggleButtonState(isChecked)
            }
        }
    }


    private fun updateToggleButtonState(attendees: List<Attendees>) {
        val currentUserId = sharedViewModel.currentUser.value?.userId.toString()
        val currentEventId = cardData.eventId.toString()
        val isAttending = attendees.any { attendee ->
            attendee.userId == currentUserId && attendee.eventId == currentEventId
        }
        Log.d("EventDetails", "User is attending: $isAttending")
        binding.attendingToggleButton.isChecked = isAttending
    }


    private fun observeEventAttendees(){
        lifecycleScope.launch {
            sharedViewModel.observeCurrentUserFromAttendees.collect { attendees ->
                Log.d("EventDetails", "Attendees updated: $attendees")
                updateToggleButtonState(attendees)
            }
        }
    }


    private fun handleToggleButtonState(isChecked: Boolean) {
        if (isChecked) {
            viewModel.addUserAsAttendee(cardData.eventId.toString(), sharedViewModel.currentUser.value?.userId.toString()) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Added to attendees", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            viewModel.removeUserAsAttendee(cardData.eventId.toString(), sharedViewModel.currentUser.value?.userId.toString()) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Removed from attendees", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to remove attendee", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showMap() {
        binding.mapView.visibility = View.VISIBLE
        eventLocation?.let { location ->
            googleMap.apply {
                addMarker(MarkerOptions().position(location).title("Event Location"))
                moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                uiSettings.isScrollGesturesEnabled = false
                uiSettings.isZoomGesturesEnabled = false
                uiSettings.isTiltGesturesEnabled = false
                uiSettings.isRotateGesturesEnabled = false

                // Set marker click listener
                mapView.setOnClickListener {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}")
                    )
                    startActivity(intent)
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "Invalid event location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseLocation(locationString: String): LatLng? {
        Log.d("location", "parseLocation: $locationString")
        return try {
            val parts = locationString.split(",")
            if (parts.size == 2) {
                val latitude = parts[0].toDoubleOrNull()
                val longitude = parts[1].toDoubleOrNull()
                if (latitude != null && longitude != null) {
                    LatLng(latitude, longitude)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        showMap()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
}
