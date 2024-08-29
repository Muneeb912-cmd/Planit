package com.example.eventmanagement.ui.fragments.manage_events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.eventmanagement.adapters.ManageEventAdapter
import com.example.eventmanagement.databinding.FragmentEventManagementBinding
import com.example.eventmanagement.di.Categories
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.ui.activities.manage_invites.ManageEventsActivity
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.add_edit_event.AddEditEventFragment
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.event_details.EventDetailsFragment
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EventManagementFragment : Fragment(), ManageEventAdapter.OnItemClickListener {

    private lateinit var binding: FragmentEventManagementBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel:EventManagementViewModel by viewModels()
    private var isUpdateEventDetailsBottomSheetShown = false

    @Inject
    @Categories
    lateinit var categories: ArrayList<String>
    private val selectedCategories = mutableSetOf<String>()
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

        binding.searchEvent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                handleQuery(s.toString().trim())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setUpAdapter() {
        val adapter =
            ManageEventAdapter(emptyList(), this)
        binding.manageEventsList.layoutManager = LinearLayoutManager(context)
        binding.manageEventsList.adapter = adapter
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            launch {
                sharedViewModel.allEvents.collect { events ->
                    (binding.manageEventsList.adapter as? ManageEventAdapter)?.updatedFilteredEvents(
                        events
                    )
                }
            }
        }
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

    private fun handleQuery(query: String) {
        val filteredEvents = sharedViewModel.allEvents.value.filter { event ->
            val matchesQuery = event.eventOrganizer?.contains(query, ignoreCase = true) == true ||
                    event.eventTitle?.contains(query, ignoreCase = true) == true
            val matchesCategory =
                selectedCategories.isEmpty() || selectedCategories.contains(event.eventCategory)
            matchesQuery && matchesCategory && event.isEventPublic == true
        }
        (binding.manageEventsList.adapter as? ManageEventAdapter)?.updatedFilteredEvents(
            filteredEvents
        )
    }


    private fun filterFeaturedEvents() {
        val filteredEvents = sharedViewModel.allEvents.value.filter { event ->
            event.isEventPublic == true &&
                    (selectedCategories.isEmpty() || selectedCategories.contains(event.eventCategory))
        }
        (binding.manageEventsList.adapter as? ManageEventAdapter)?.updatedFilteredEvents(
            filteredEvents
        )

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
        viewModel.deleteEventById(cardData.eventId.toString(),true){result->
            if(result){
                Toast.makeText(requireContext(),"Event Deleted!",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),"Event Deleted!",Toast.LENGTH_SHORT).show()
            }
        }
    }


}