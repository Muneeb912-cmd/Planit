package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.eventmanagement.databinding.FragmentEventDetailsBinding
import com.example.eventmanagement.models.CardData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EventDetailsFragment(private val cardData: CardData) : BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentEventDetailsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: com.google.android.gms.maps.MapView
    private var eventLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        eventLocation = cardData.eventLocation?.let { parseLocation(it) }

        binding.attendingToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(requireContext(),"User going to Event",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(),"User not going to Event",Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            eventTitle.text=cardData.eventTitle
            eventTime.text=cardData.eventTiming
            eventDate.text=cardData.eventDate
            eventOrganizer.text="Organizer: ${cardData.eventOrganizer}"
            eventDescription.text=cardData.eventDescription
            eventCategory.text="Category: ${cardData.eventCategory}"
            eventStatus.text="Status: ${cardData.eventStatus}"
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
            if(cardData.eventStatus=="Missed") {
                attendingTv.text = "You missed the Event!"
                attendingToggleButton.visibility=View.GONE
            }
        }
    }
    private fun showMap() {
        binding.mapView.visibility = View.VISIBLE
        eventLocation?.let { location ->
            googleMap.addMarker(MarkerOptions().position(location).title("Event Location"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

            googleMap.setOnMarkerClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}"))
                startActivity(intent)
                true
            }
        } ?: run {
            Toast.makeText(requireContext(), "Invalid event location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseLocation(locationString: String): LatLng? {
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
}
