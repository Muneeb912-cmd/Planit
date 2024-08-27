package com.example.eventmanagement.ui.fragments.events

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.FeaturedEventAdapter
import com.example.eventmanagement.adapters.PopularEventCardAdapter
import com.example.eventmanagement.databinding.FragmentEventsBinding
import com.example.eventmanagement.di.Categories
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details.EventDetailsFragment
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment(),
    PopularEventCardAdapter.EventCardClickListener,
    FeaturedEventAdapter.OnFeaturedEventClickListener {

    private lateinit var binding: FragmentEventsBinding
    private var isEventDetailsBottomSheetShown = false

    @Inject
    @Categories
    lateinit var categories: ArrayList<String>

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val eventsViewModel: EventsViewModel by viewModels()
    private val selectedCategories = mutableSetOf<String>()

    private var isUiHidden = false

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

        binding.expandUi.setImageResource(R.drawable.ic_up)

        binding.expandUi.setOnClickListener {
            if (isUiHidden) {
                binding.expandUi.setImageResource(R.drawable.ic_up)
                showUiElements("expandUiBtn")
            } else {
                binding.expandUi.setImageResource(R.drawable.ic_down)
                hideUiElements("expandUiBtn")
            }
        }

        binding.searchEvent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                handleSearch(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setUpCategories() {
        categories.forEach { label ->
            val chip = Chip(context).apply {
                text = label
                isClickable = true
                isCheckable = true
                setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.chip_margin_start)
                    marginEnd = resources.getDimensionPixelSize(R.dimen.chip_margin_end)
                    topMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_top)
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_bottom)
                }
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategories.add(label)
                        setChipBackgroundColorResource(R.color.md_theme_primary)
                        setTextColor(resources.getColor(R.color.md_theme_surfaceContainerLow, null))
                    } else {
                        selectedCategories.remove(label)
                        setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                        setTextColor(
                            resources.getColor(
                                R.color.md_theme_onBackground_highContrast,
                                null
                            )
                        )
                    }
                    filterFeaturedEvents()
                }
            }
            binding.chipsContainer.addView(chip)
        }
    }

    private fun setUpRecyclerViews() {
        binding.recyclerViewEventCards.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewEventCards.adapter = PopularEventCardAdapter(
            emptyList(), sharedViewModel.allFavEvents.value, this
        )

        binding.recyclerViewPromotionCards.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewPromotionCards.adapter = FeaturedEventAdapter(
            emptyList(), sharedViewModel.allFavEvents.value, this
        )

        binding.searchItemRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.searchItemRecyclerView.adapter = PopularEventCardAdapter(
            emptyList(), sharedViewModel.allFavEvents.value, this
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            launch { sharedViewModel.allEvents.collect { filterFeaturedEvents() } }
            launch { sharedViewModel.allFavEvents.collect {favEvents->
                (binding.recyclerViewPromotionCards.adapter as? FeaturedEventAdapter)?.submitFavEventsList(
                    favEvents
                )
                (binding.recyclerViewEventCards.adapter as? PopularEventCardAdapter)?.submitFavEventsList(
                    favEvents
                )
            }}
        }
    }

    private fun filterFeaturedEvents() {
        val filteredEvents = sharedViewModel.allEvents.value.filter { event ->
            event.isEventFeatured == true &&
                    event.isEventPublic == true &&
                    (selectedCategories.isEmpty() || selectedCategories.contains(event.eventCategory))
        }
        (binding.recyclerViewPromotionCards.adapter as? FeaturedEventAdapter)?.submitList(
            filteredEvents
        )

        val filteredPopularEvents = sharedViewModel.allEvents.value.filter { event ->
            event.isEventPopular == true &&
                    event.isEventPublic == true &&
                    (selectedCategories.isEmpty() || selectedCategories.contains(event.eventCategory))
        }
        (binding.recyclerViewEventCards.adapter as? PopularEventCardAdapter)?.submitList(
            filteredPopularEvents
        )

    }

    private fun handleSearch(query: String) {
        if (query.isEmpty()) {
            showUiElements("search")
            filterFeaturedEvents()
        } else {
            hideUiElements("search")
            deselectAllCategories()
            handleQuery(query)
        }
    }

    private fun handleQuery(query: String) {
        val searchedEvents = sharedViewModel.allEvents.value.filter { event ->
            val matchesQuery = event.eventOrganizer?.lowercase()?.contains(query.lowercase()) == true ||
                    event.eventTitle?.lowercase()?.contains(query.lowercase()) == true
            matchesQuery &&
                    event.isEventPublic == true &&
                    (selectedCategories.isEmpty() || selectedCategories.contains(event.eventCategory))
        }
        (binding.searchItemRecyclerView.adapter as? PopularEventCardAdapter)?.submitList(searchedEvents)
    }

    private fun deselectAllCategories() {
        selectedCategories.clear()
        (0 until binding.chipsContainer.childCount).forEach { index ->
            (binding.chipsContainer.getChildAt(index) as? Chip)?.isChecked = false
        }
    }

    private fun hideUiElements(key: String) {
        if (key != "search") {
            animateViewVisibility(binding.searchEventContainer, View.GONE)
            animateViewVisibility(binding.titlePopularEvents, View.GONE)
            animateViewVisibility(binding.recyclerViewEventCards, View.GONE)
            isUiHidden = true
        } else {
            animateViewVisibility(binding.searchItemRecyclerView, View.VISIBLE)
            animateViewVisibility(binding.titlePopularEvents, View.GONE)
            animateViewVisibility(binding.recyclerViewEventCards, View.GONE)
            animateViewVisibility(binding.featuredEventTitle, View.GONE)
            animateViewVisibility(binding.recyclerViewPromotionCards, View.GONE)
            animateViewVisibility(binding.expandLayout, View.GONE)
        }
    }

    private fun showUiElements(key: String) {
        if (key != "search") {
            animateViewVisibility(binding.searchEventContainer, View.VISIBLE)
            animateViewVisibility(binding.titlePopularEvents, View.VISIBLE)
            animateViewVisibility(binding.recyclerViewEventCards, View.VISIBLE)
            isUiHidden = false
        } else {
            animateViewVisibility(binding.searchItemRecyclerView, View.GONE)
            animateViewVisibility(binding.titlePopularEvents, View.VISIBLE)
            animateViewVisibility(binding.recyclerViewEventCards, View.VISIBLE)
            animateViewVisibility(binding.featuredEventTitle, View.VISIBLE)
            animateViewVisibility(binding.recyclerViewPromotionCards, View.VISIBLE)
            animateViewVisibility(binding.expandLayout, View.VISIBLE)
        }
    }


    private fun animateViewVisibility(view: View, visibility: Int) {
        val duration = 300L
        view.animate()
            .translationY(if (visibility == View.VISIBLE) 0f else -view.height.toFloat())
            .alpha(if (visibility == View.VISIBLE) 1f else 0f)
            .setDuration(duration)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                view.visibility = visibility
            }
    }

    override fun onEventCardClick(cardData: EventData) {
        showEventDetails(cardData)
    }

    override fun onFeaturedEventCardClick(cardData: EventData) {
        showEventDetails(cardData)
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

    override fun onFavIconClick(cardData: EventData) {
        val userId = sharedViewModel.currentUser.value?.userId.toString()
        if (sharedViewModel.allFavEvents.value.any { it.eventId == cardData.eventId }) {
            removeEventFromFavorites(userId, cardData)
        } else {
            addEventToFavorites(userId, cardData)
        }
    }

    private fun removeEventFromFavorites(userId: String, cardData: EventData) {
        eventsViewModel.removeEventFromUserFav(userId, cardData) { result, msg ->
            showToast("Event deleted from favorites", result, msg)
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
}
