package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.add_edit_event

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.PlaceAdapter
import com.example.eventmanagement.databinding.ActivityLocationSearchBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class LocationSearchActivity : AppCompatActivity(), OnMapReadyCallback, PlaceAdapter.OnItemClickListener {

    private lateinit var placesClient: PlacesClient
    private lateinit var binding: ActivityLocationSearchBinding
    private lateinit var placeAdapter: PlaceAdapter
    private var googleMap: GoogleMap? = null

    private var selectedLatLng: LatLng? = null
    private var selectedPlaceName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityLocationSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyB7EIFMRQe-9Ijp2e-Bjwy6PnZ6ksByABg")
        }
        placesClient = Places.createClient(this)

        // Set up RecyclerView and Adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        placeAdapter = PlaceAdapter(this)
        binding.recyclerView.adapter = placeAdapter

        // Set up SearchView listener
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    performPlaceSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    performPlaceSearch(newText)
                    binding.recyclerView.visibility = View.VISIBLE
                } else {
                    placeAdapter.submitList(emptyList())
                    binding.recyclerView.visibility = View.GONE
                    binding.doneBtn.isClickable = false
                    binding.doneBtn.setBackgroundColor(resources.getColor(R.color.md_theme_outlineVariant, null))
                }
                return true
            }
        })

        // Set up done button click listener
        binding.doneBtn.setOnClickListener {
            selectedLatLng?.let { latLng ->
                val intent = Intent().apply {
                    putExtra("location_name", selectedPlaceName)
                    putExtra("latitude", latLng.latitude)
                    putExtra("longitude", latLng.longitude)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        // Initialize the MapView
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        // Set SearchView to be expanded by default
        binding.searchView.isIconified = false
        binding.searchView.requestFocus()
    }

    private fun performPlaceSearch(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                placeAdapter.submitList(predictions)
            }
            .addOnFailureListener { exception ->
                Log.e("LocationSearch", "Place search failed", exception)
            }
    }

    override fun onItemClick(placeId: String, placeName: String) {
        val placeFields = listOf(Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                if (latLng != null) {
                    selectedLatLng = latLng
                    selectedPlaceName = placeName

                    binding.searchView.setQuery(placeName, false)
                    binding.recyclerView.visibility = View.GONE
                    binding.doneBtn.isClickable = true
                    binding.doneBtn.setBackgroundColor(resources.getColor(R.color.md_theme_primary, null))
                    updateMap(latLng, placeName)
                } else {
                    Log.e("LocationSearch", "LatLng not available for place: $placeId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LocationSearch", "Error fetching place details: ", exception)
            }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val lahoreLatLng = LatLng(31.5204, 74.3587)
        updateMap(lahoreLatLng, "Lahore, Pakistan")
    }

    private fun updateMap(latLng: LatLng, placeName: String) {
        googleMap?.let {
            it.clear()
            it.addMarker(MarkerOptions().position(latLng).title(placeName))
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
