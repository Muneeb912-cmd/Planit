package com.example.eventmanagement.ui.fragments.my_events

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.adapters.MyEventCardAdapter
import com.example.eventmanagement.databinding.FragmentMyEventsBinding
import com.example.eventmanagement.models.CardData
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details.EventDetailsFragment
import com.google.android.material.tabs.TabLayout


class MyEventsFragment : Fragment(), MyEventCardAdapter.OnMyEventClickListener {
    private lateinit var binding: FragmentMyEventsBinding
    private val eventCardList = listOf(
        CardData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-10",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "On-Going"
        ),
        CardData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-14",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "On-Going"
        ),
        CardData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-20",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Up-Coming"
        ),
        CardData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-21",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Up-Coming"
        ),
        CardData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-20",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Up-Coming"
        ),
        CardData(
            eventTitle = "Featured Event",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-15",
            isEventFeatured = true,
            isEventPopular = false,
            isEventPublic = true,
            numberOfPeopleAttending = 100,
            eventStatus = "Missed"
        ),
        CardData(
            eventTitle = "Popular Event 1",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-01",
            isEventFeatured = false,
            isEventPopular = true,
            isEventPublic = true,
            numberOfPeopleAttending = 28,
            eventStatus = "Missed"
        ),
        CardData(
            eventTitle = "Popular Event 2",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "37.7749,-122.4194",
            eventDate = "2024-08-14",
            isEventFeatured = false,
            isEventPopular = true,
            isEventPublic = true,
            numberOfPeopleAttending = 14,
            eventStatus = "Missed"
        ),
    )
    private var filteredEvents = listOf<CardData>()
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
            eventCardList,
            this
        )
        binding.eventsList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.eventsList.adapter = adapter
    }

    override fun OnMyEventCardClickListener(cardData: CardData) {
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
            0 -> eventCardList.filter { it.eventStatus == "On-Going" }
            1 -> eventCardList.filter { it.eventStatus == "Up-Coming" }
            2 -> eventCardList.filter { it.eventStatus == "Missed" }
            else -> eventCardList
        }
        updateRecyclerView(filteredEvents)
    }

    private fun updateRecyclerView(events: List<CardData>) {
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