package com.example.eventmanagement.ui.activities.fav_events

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.databinding.ActivityFavEventBinding
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.bottomsheets.eventdetails.EventDetailsFragment
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavEventActivity : AppCompatActivity(), FavEventAdapter.EventCardClickListener {

    private val favEventViewModel: FavEventsViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var binding: ActivityFavEventBinding
    private var isEventDetailsBottomSheetShown = false
    private lateinit var adapter: FavEventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpFavEventsDisplay()
        observeFavEvents()
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeFavEvents() {
        lifecycleScope.launch {
            // Combine both flows to react to changes in either
            sharedViewModel.allFavEvents.combine(sharedViewModel.allEvents) { allFavEvents, allEvents ->
                Pair(allFavEvents, allEvents)
            }.collect { (allFavEvents, allEvents) ->
                if (allFavEvents.isNotEmpty() && allEvents.isNotEmpty()) {
                    val filteredFavEvents = allEvents.filter { event ->
                        allFavEvents.contains(event.eventId)
                    }
                    if (filteredFavEvents.isNotEmpty()) {
                        binding.favEventsList.visibility = View.VISIBLE
                        binding.noEventTv.visibility = View.GONE
                        adapter.updatedFavEvents(filteredFavEvents)
                    } else {
                        binding.favEventsList.visibility = View.GONE
                        binding.noEventTv.visibility = View.VISIBLE
                    }
                } else {
                    binding.favEventsList.visibility = View.GONE
                    binding.noEventTv.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun setUpFavEventsDisplay() {
        adapter = FavEventAdapter(emptyList(), this)
        binding.favEventsList.layoutManager = LinearLayoutManager(this)
        binding.favEventsList.adapter = adapter
    }

    override fun onEventCardClick(cardData: EventData) {
        if (!isEventDetailsBottomSheetShown) {
            isEventDetailsBottomSheetShown = true

            val bottomSheetFragment = EventDetailsFragment(cardData)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

            bottomSheetFragment.setOnDismissListener {
                isEventDetailsBottomSheetShown = false
            }
        }
    }

    override fun onFavIconClick(cardData: EventData) {
        val currentUserId = sharedViewModel.currentUser.value?.userId.toString()
        favEventViewModel.removeEventFromUserFav(currentUserId, cardData) { result, msg ->
            if (result) {
                Toast.makeText(this, "Event removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error: $msg", Toast.LENGTH_SHORT).show()
            }
        }
    }
}