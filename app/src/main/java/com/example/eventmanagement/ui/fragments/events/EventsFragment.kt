// EventsFragment.kt
package com.example.eventmanagement.ui.fragments.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.CarousalAdapter
import com.example.eventmanagement.databinding.FragmentEventsBinding
import com.example.eventmanagement.models.CardData
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment() {
    private lateinit var binding: FragmentEventsBinding

    @Inject
    lateinit var categories: ArrayList<String>

    private val cardList = listOf(
        CardData(
            eventTitle = "Shaheer's Happy Birthday",
            eventOrganizer = "Devsinc",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "Devsinc, First Floor",
        ),
        CardData(
            eventTitle = "Shaheer's Happy Birthday1",
            eventOrganizer = "Devsinc1",
            eventTiming = "2:00 PM - 3:00 PM",
            eventCategory = "Conferences",
            eventDescription = "Today we will celebrate Shaheer's Birthday",
            eventLocation = "Devsinc, First Floor"
        ),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpCategories()
        setUpRecyclerView()
    }

    private fun setUpCategories() {
        val chipLabels = categories

        for (label in chipLabels) {
            val chip = Chip(context).apply {
                text = label
                isClickable = true
                isCheckable = true
                setChipBackgroundColorResource(R.color.md_theme_primaryFixed) // Default background color
                setTextColor(resources.getColor(R.color.md_theme_onBackground_highContrast, null))

                // Set margins
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = resources.getDimensionPixelSize(R.dimen.chip_margin_start)
                    marginEnd = resources.getDimensionPixelSize(R.dimen.chip_margin_end)
                    topMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_top)
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.chip_margin_bottom)
                }
                layoutParams = params

                // Handle chip selection
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        setChipBackgroundColorResource(R.color.md_theme_primary)
                        setTextColor(resources.getColor(R.color.md_theme_surfaceContainerLow, null))
                    } else {
                        setChipBackgroundColorResource(R.color.md_theme_primaryFixed)
                        setTextColor(
                            resources.getColor(
                                R.color.md_theme_onBackground_highContrast,
                                null
                            )
                        )
                    }
                }
            }
            binding.chipsContainer.addView(chip)
        }
    }

    private fun setUpRecyclerView() {
        val adapter = CarousalAdapter(cardList)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
        startAutoScroll()
    }

    private fun startAutoScroll() {
        val handler = android.os.Handler()
        val scrollRunnable = object : Runnable {
            override fun run() {
                val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
                val nextPosition =
                    (layoutManager.findFirstVisibleItemPosition() + 1) % cardList.size
                binding.recyclerView.smoothScrollToPosition(nextPosition)
                handler.postDelayed(this, 3000) // Scroll every 3 seconds
            }
        }
        handler.postDelayed(scrollRunnable, 3000)
    }
}
