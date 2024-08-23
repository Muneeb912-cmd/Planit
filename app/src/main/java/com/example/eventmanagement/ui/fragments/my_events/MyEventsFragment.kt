package com.example.eventmanagement.ui.fragments.my_events

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.adapters.MyEventCardAdapter
import com.example.eventmanagement.databinding.FragmentMyEventsBinding
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details.EventDetailsFragment
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch


class MyEventsFragment : Fragment(), MyEventCardAdapter.OnMyEventClickListener {
    private lateinit var binding: FragmentMyEventsBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var filteredEvents = listOf<EventData>()
    private var currentTabPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayout()
        setUpMyEventsCardView()
        binding.tabLayout.getTabAt(0)?.select()
        filterEvents(0)
        setupSearchListener()
        lifecycleScope.launch {
            sharedViewModel.allEvents.collect {
                setUpMyEventsCardView()
            }
        }
    }

    private fun setupSearchListener() {
        binding.searchEvent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchEvents()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun setUpMyEventsCardView() {
        val adapter = MyEventCardAdapter(
            sharedViewModel.allEvents.value,
            this
        )
        binding.eventsList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.eventsList.adapter = adapter
    }

    override fun OnMyEventCardClickListener(cardData: EventData) {
        val bottomSheetFragment = EventDetailsFragment(cardData)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentTabPosition = it.position
                    filterEvents(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterEvents(it.position)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterEvents(it.position)
                }
            }
        })
    }

    private fun filterEvents(selectedTabPosition: Int) {
        filteredEvents = when (selectedTabPosition) {
            0 -> sharedViewModel.allEvents.value.filter { it.eventStatus == "On-Going" }
            1 -> sharedViewModel.allEvents.value.filter { it.eventStatus == "Up-Coming" }
            2 -> sharedViewModel.allEvents.value.filter { it.eventStatus == "Missed" }
            else -> sharedViewModel.allEvents.value
        }
        updateRecyclerView(filteredEvents)
    }

    private fun updateRecyclerView(events: List<EventData>) {
        val adapter = MyEventCardAdapter(events, this)
        binding.eventsList.adapter = adapter
    }

    private fun searchEvents() {
        val query = binding.searchEvent.text.toString().trim().lowercase()
        filteredEvents = if (query.isEmpty()) {
            filterEvents(currentTabPosition)
            filteredEvents
        } else {
            filteredEvents.filter {
                it.eventOrganizer?.lowercase()?.contains(query) == true ||
                        it.eventTitle?.lowercase()?.contains(query) == true
            }
        }
        updateRecyclerView(filteredEvents)
    }

}