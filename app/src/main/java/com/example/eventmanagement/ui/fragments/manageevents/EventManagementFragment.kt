package com.example.eventmanagement.ui.fragments.manageevents

import android.content.Intent
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
import com.example.eventmanagement.databinding.FragmentEventManagementBinding
import com.example.eventmanagement.di.StaticDataModule
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.activities.manage_invites.ManageEventsActivity
import com.example.eventmanagement.ui.bottomsheets.addeditevent.AddEditEventFragment
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventManagementFragment : Fragment(), ManageEventAdapter.OnItemClickListener {

    private lateinit var binding: FragmentEventManagementBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel: EventManagementViewModel by viewModels()
    private var isUpdateEventDetailsBottomSheetShown = false

    private val categories = StaticDataModule.provideCategories()
    private val selectedCategories = mutableSetOf<String>()
    private var currentSelectedTabIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        observeEvents()
        setUpCategories()
        handleSearchBar()
        handleTabsSelection()

        // Set initial tab
        binding.tabLayout.getTabAt(0)?.select()
        filterEvents()
    }

    private fun handleSearchBar() {
        binding.searchEvent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterEvents()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun handleTabsSelection() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentSelectedTabIndex = it.position
                    filterEvents()
                    clearSearchAndCategories()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setUpAdapter() {
        val adapter = ManageEventAdapter(emptyList(), this)
        binding.manageEventsList.layoutManager = LinearLayoutManager(context)
        binding.manageEventsList.adapter = adapter
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            sharedViewModel.allEvents.collect {
                filterEvents() // Filter events initially
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
                    filterEvents()
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


    private fun filterEvents() {
        val filteredEvents = sharedViewModel.allEvents.value.filter { event ->
            val matchesQuery = binding.searchEvent.text.toString().trim().let { query ->
                event.eventOrganizer?.contains(query, ignoreCase = true) == true ||
                        event.eventTitle?.contains(query, ignoreCase = true) == true
            }
            val matchesCategory =
                selectedCategories.contains("All") || selectedCategories.contains(event.eventCategory)
            val matchesTab = when (currentSelectedTabIndex) {
                0 -> event.eventStatus != "Missed"
                1 -> event.eventStatus == "Missed"
                else -> true
            }
            matchesQuery && matchesCategory && event.isEventPublic == true && matchesTab
        }
        (binding.manageEventsList.adapter as? ManageEventAdapter)?.updatedFilteredEvents(filteredEvents)
    }

    private fun clearSearchAndCategories() {
        binding.searchEvent.text?.clear()
        checkAllCategoryChip()
    }

    override fun onItemClick(cardData: EventData) {
        if (!isUpdateEventDetailsBottomSheetShown) {
            isUpdateEventDetailsBottomSheetShown = true

            val bottomSheetFragment = AddEditEventFragment()
            val bundle = Bundle().apply {
                putString("key", "update")
                putSerializable("eventData", cardData)
            }
            bottomSheetFragment.arguments = bundle
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.setOnDismissListener {
                isUpdateEventDetailsBottomSheetShown = false
            }
        }
    }

    override fun onCreateInviteClick(cardData: EventData) {
        val eventId = cardData.eventId
        eventId?.let {
            val intent = Intent(requireContext(), ManageEventsActivity::class.java).apply {
                putExtra(ManageEventsActivity.EXTRA_EVENT_ID, it)
            }
            startActivity(intent)
        }
    }

    override fun onDeleteInviteClick(cardData: EventData) {
        viewModel.deleteEventById(cardData) { result ->
            if (result) {
                Toast.makeText(requireContext(), "Event Deleted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Event Deletion Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
