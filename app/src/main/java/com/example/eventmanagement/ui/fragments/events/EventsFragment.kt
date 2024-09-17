package com.example.eventmanagement.ui.fragments.events

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentEventsBinding
import com.example.eventmanagement.di.StaticDataModule
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.bottomsheets.eventdetails.EventDetailsFragment
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventsFragment : Fragment(), AllEventCardAdapter.EventCardClickListener {

    private lateinit var binding: FragmentEventsBinding
    private var isEventDetailsBottomSheetShown = false

    private val categories = StaticDataModule.provideCategories()

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by viewModels()
    private val selectedCategories = mutableSetOf<String>()
    private lateinit var eventsAdapter: AllEventCardAdapter
    private var selectedFilter: String = "All"
    private var searchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCategories()
        setUpRecyclerViews()
        observeViewModel()
        setUpEventFilterSpinner()

        binding.searchEvent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim()
                filterAllEvents()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                    binding.searchEvent.clearFocus()
                }
            }
        })
    }

    private fun setUpEventFilterSpinner() {
        val filterOptions = arrayOf("All", "Up-Coming", "On-Going", "Missed", "Popular", "Featured", "Favorites")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.eventFilterSpinner.adapter = adapter
        binding.eventFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedFilter = filterOptions[position]
                filterAllEvents()
                binding.searchEvent.clearFocus()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Default behavior: No filter applied
            }
        }
    }

    private fun setUpCategories() {
        selectedCategories.add("All")

        categories.forEach { label ->
            val chip = Chip(context).apply {
                text = label
                isClickable = true
                isCheckable = true

                if (label == "All") {
                    isChecked = true
                    setChipBackgroundColorResource(R.color.md_theme_primary)
                    setTextColor(resources.getColor(R.color.md_theme_surfaceContainerLow, null))
                } else {
                    setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                    setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))
                }

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.chip_margin_start)
                    marginEnd = resources.getDimensionPixelSize(R.dimen.chip_margin_end)
                    topMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_top)
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_bottom)
                }

                setOnCheckedChangeListener { _, isChecked ->
                    if (label == "All") {
                        if (isChecked) {
                            selectedCategories.clear()
                            selectedCategories.add("All")
                            uncheckAllExcept(label)
                        }
                    } else {
                        if (isChecked) {
                            selectedCategories.remove("All")
                            selectedCategories.add(label)
                            uncheckCategory("All")
                            setChipBackgroundColorResource(R.color.md_theme_primary)
                            setTextColor(resources.getColor(R.color.md_theme_surfaceContainerLow, null))
                        } else {
                            selectedCategories.remove(label)
                            if (selectedCategories.isEmpty()) {
                                selectedCategories.add("All")
                                checkAllCategoryChip()
                            }
                            setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                            setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))
                        }
                    }
                    filterAllEvents()
                }
            }
            binding.chipsContainer.addView(chip)
        }
    }

    private fun checkAllCategoryChip() {
        for (i in 0 until binding.chipsContainer.childCount) {
            val chip = binding.chipsContainer.getChildAt(i) as? Chip
            if (chip != null && chip.text == "All") {
                chip.isChecked = true
                break
            }
        }
    }

    private fun uncheckCategory(category: String) {
        for (i in 0 until binding.chipsContainer.childCount) {
            val chip = binding.chipsContainer.getChildAt(i) as Chip
            if (chip.text == category) {
                chip.isChecked = false
                chip.setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                chip.setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))
            }
        }
    }

    private fun uncheckAllExcept(exceptCategory: String) {
        for (i in 0 until binding.chipsContainer.childCount) {
            val chip = binding.chipsContainer.getChildAt(i) as Chip
            if (chip.text != exceptCategory) {
                chip.isChecked = false
                chip.setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                chip.setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))
            } else {
                chip.isChecked = true
                chip.setChipBackgroundColorResource(R.color.md_theme_primary)
                chip.setTextColor(resources.getColor(R.color.md_theme_surfaceContainerLow, null))
            }
        }
    }

    private fun setUpRecyclerViews() {
        eventsAdapter = AllEventCardAdapter(
            emptyList(), sharedViewModel.allFavEvents.value, this
        )
        binding.searchItemRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.searchItemRecyclerView.adapter = eventsAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            sharedViewModel.allEvents.collect {
                filterAllEvents()
            }
        }
        lifecycleScope.launch {
            sharedViewModel.allFavEvents.collect { events ->
                eventsAdapter.submitFavEventsList(events)
                filterAllEvents()
            }
        }
    }

    private fun filterAllEvents() {
        val allEvents = sharedViewModel.allEvents.value
        val favEvents = sharedViewModel.allFavEvents.value

        val filteredEvents = when (selectedFilter) {
            "All" -> allEvents
            "Up-Coming" -> allEvents.filter { it.isUpcoming() }
            "On-Going" -> allEvents.filter { it.isOngoing() }
            "Missed" -> allEvents.filter { it.isMissed() }
            "Popular" -> allEvents.filter { it.isEventPopular == true }
            "Featured" -> allEvents.filter { it.isEventFeatured == true }
            "Favorites" -> allEvents.filter { event -> favEvents.contains(event.eventId) }
            else -> emptyList()
        }.filter { event ->
            val matchesCategory = selectedCategories.contains("All") || selectedCategories.contains(event.eventCategory)
            val matchesQuery = event.eventOrganizer?.contains(searchQuery, ignoreCase = true) == true ||
                    event.eventTitle?.contains(searchQuery, ignoreCase = true) == true

            matchesCategory && matchesQuery && event.isEventPublic == true
        }

        (binding.searchItemRecyclerView.adapter as? AllEventCardAdapter)?.submitList(filteredEvents)
    }

    private fun handleQuery(query: String) {
        // Already handled in filterAllEvents as searchQuery is updated in afterTextChanged
    }

    override fun onEventCardClick(cardData: EventData) {
        showEventDetails(cardData)
    }

    override fun onFavIconClick(cardData: EventData) {
        val userId = sharedViewModel.currentUser.value?.userId.toString()
        if (sharedViewModel.allFavEvents.value.any { it == cardData.eventId }) {
            removeEventFromFavorites(userId, cardData)
        } else {
            addEventToFavorites(userId, cardData)
        }
    }

    private fun removeEventFromFavorites(userId: String, cardData: EventData) {
        eventsViewModel.removeEventFromUserFav(userId, cardData) { result, msg ->
            showToast("Event removed from favorites", result, msg)
        }
    }

    private fun addEventToFavorites(userId: String, cardData: EventData) {
        eventsViewModel.addEventToUserFav(userId, cardData) { result, msg ->
            showToast("Event added to favorites", result, msg)
        }
    }

    private fun showToast(message: String, result: Boolean, msg: String) {
        Toast.makeText(requireContext(), if (result) message else "Error: $msg", Toast.LENGTH_SHORT)
            .show()
    }

    private fun showEventDetails(cardData: EventData) {
        if (!isEventDetailsBottomSheetShown) {
            isEventDetailsBottomSheetShown = true
            val bottomSheetFragment = EventDetailsFragment(cardData)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.setOnDismissListener {
                isEventDetailsBottomSheetShown = false
            }
        }
    }

    private fun EventData.isUpcoming(): Boolean {
        return this.eventStatus == "Up-Coming"
    }

    private fun EventData.isOngoing(): Boolean {
        return this.eventStatus == "On-Going"
    }

    private fun EventData.isMissed(): Boolean {
        return this.eventStatus == "Missed"
    }
}
