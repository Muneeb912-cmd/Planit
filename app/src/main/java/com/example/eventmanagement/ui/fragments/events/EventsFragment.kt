package com.example.eventmanagement.ui.fragments.events


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.PopularEventCardAdapter
import com.example.eventmanagement.adapters.FeaturedEventAdapter
import com.example.eventmanagement.databinding.FragmentEventsBinding
import com.example.eventmanagement.di.Categories
import com.example.eventmanagement.models.CardData
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details.EventDetailsFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment(), PopularEventCardAdapter.EventCardClickListener,
    FeaturedEventAdapter.OnFeaturedEventClickListener {
    private lateinit var binding: FragmentEventsBinding

    @Inject
    @Categories
    lateinit var categories: ArrayList<String>
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCategories()
        setUpEventRecyclerView()
        setUpPromotionRecyclerView()
        setUpScrollListener()
    }

    private fun setUpCategories() {
        val chipLabels = categories
        for (label in chipLabels) {
            val chip = Chip(context).apply {
                text = label
                isClickable = true
                isCheckable = true
                setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.chip_margin_start)
                    marginEnd = resources.getDimensionPixelSize(R.dimen.chip_margin_end)
                    topMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_top)
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_bottom)
                }
                layoutParams = params

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        setChipBackgroundColorResource(R.color.md_theme_primary)
                        setTextColor(resources.getColor(R.color.md_theme_surfaceContainerLow, null))
                    } else {
                        setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                        setTextColor(
                            resources.getColor(
                                R.color.md_theme_onBackground_highContrast, null
                            )
                        )
                    }
                }
            }
            binding.chipsContainer.addView(chip)
        }
    }

    private fun setUpEventRecyclerView() {
        val adapter = PopularEventCardAdapter(
            eventCardList.filter { it.isEventPopular == true && it.isEventPublic == true },
            this
        )
        binding.recyclerViewEventCards.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewEventCards.adapter = adapter
        startAutoScrollForEvents()
    }

    private fun setUpPromotionRecyclerView() {
        val adapter = FeaturedEventAdapter(
            eventCardList.filter { it.isEventFeatured == true && it.isEventPublic == true },
            this
        )
        binding.recyclerViewPromotionCards.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewPromotionCards.adapter = adapter
    }

    private fun startAutoScrollForEvents() {
        val handler = android.os.Handler()
        val scrollRunnable = object : Runnable {
            override fun run() {
                val layoutManager = binding.recyclerViewEventCards.layoutManager as LinearLayoutManager
                val currentPosition = layoutManager.findFirstVisibleItemPosition()
                val totalItemCount = binding.recyclerViewEventCards.adapter?.itemCount ?: 0

                if (totalItemCount > 0) {
                    val nextPosition = (currentPosition + 1) % totalItemCount
                    binding.recyclerViewEventCards.smoothScrollToPosition(nextPosition)
                    handler.postDelayed(this, 3000) // Repeat after 3 seconds
                }
            }
        }
        handler.postDelayed(scrollRunnable, 3000)
    }

    private fun setUpScrollListener() {
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val scrollThreshold = 100
                Log.d("Scroll", "onScrolled: $dy")
                if (dy > scrollThreshold) { // Scrolling down
                    hideSearchAndEventCards()
                } else if (dy < -scrollThreshold) { // Scrolling up
                    showSearchAndEventCards()
                }
            }
        }

        binding.recyclerViewPromotionCards.addOnScrollListener(scrollListener)
    }

    private fun hideSearchAndEventCards() {
        binding.searchEventContainer.animate()
            .translationY(-binding.searchEventContainer.height.toFloat()).alpha(0f).setDuration(300)
            .withEndAction {
                binding.searchEventContainer.visibility = View.GONE
            }

        binding.titlePopularEvents.animate()
            .translationY(-binding.titlePopularEvents.height.toFloat()).alpha(0f).setDuration(300)
            .withEndAction {
                binding.titlePopularEvents.visibility = View.GONE
            }

        binding.recyclerViewEventCards.animate()
            .translationY(-binding.recyclerViewEventCards.height.toFloat()).alpha(0f)
            .setDuration(300).withEndAction {
                binding.recyclerViewEventCards.visibility = View.GONE
            }
    }

    private fun showSearchAndEventCards() {
        binding.searchEventContainer.visibility = View.VISIBLE
        binding.searchEventContainer.animate().translationY(0f).alpha(1f).setDuration(300)

        binding.titlePopularEvents.visibility = View.VISIBLE
        binding.titlePopularEvents.animate().translationY(0f).alpha(1f).setDuration(300)

        binding.recyclerViewEventCards.visibility = View.VISIBLE
        binding.recyclerViewEventCards.animate().translationY(0f).alpha(1f).setDuration(300)
    }

    override fun onEventCardClick(cardData: CardData) {
        val bottomSheetFragment = EventDetailsFragment(cardData)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    override fun onFeaturedEventCardClick(cardData: CardData) {
        val bottomSheetFragment = EventDetailsFragment(cardData)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }
}