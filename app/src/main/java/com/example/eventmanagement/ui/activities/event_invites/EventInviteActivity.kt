package com.example.eventmanagement.ui.activities.event_invites

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.databinding.ActivityEventInviteBinding
import com.example.eventmanagement.models.EventData
import com.example.eventmanagement.models.EventInvites
import com.example.eventmanagement.ui.bottomsheets.eventdetails.EventDetailsFragment
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventInviteActivity : AppCompatActivity(), EventInviteAdapter.EventCardClickListener {

    private lateinit var binding: ActivityEventInviteBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private val viewModel: EventInviteViewModel by viewModels()
    private var isEventDetailsBottomSheetShown = false
    private var currentTabPosition = 0
    private lateinit var adapter: EventInviteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventInviteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupTabLayout()
        setupRecyclerView()
        observeInviteEvents()

        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeInviteEvents() {
        lifecycleScope.launch {
            sharedViewModel.currentUserInvites.collect { eventInvites ->
                Log.d("EventInvites", "observeInviteEvents: $eventInvites")
                handleInviteVisibility(eventInvites.isEmpty())
                updateTabTitles(eventInvites)
                filterEvents(currentTabPosition)
            }
        }
    }


    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentTabPosition = it.position
                    filterEvents(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterEvents(it.position)
                }
            }
        })
    }


    private fun filterEvents(selectedTabPosition: Int) {
        val status = when (selectedTabPosition) {
            0 -> "pending"
            1 -> "accepted"
            2 -> "expired"
            else -> null
        }

        val filteredEvents = sharedViewModel.currentUserInvites.value.filter { invite ->
            status == null || invite.inviteStatus == status
        }

        adapter.updateData(filteredEvents)
        handleInviteVisibility(filteredEvents.isEmpty(), selectedTabPosition)
    }

    private fun handleInviteVisibility(
        isEmpty: Boolean,
        selectedTabPosition: Int = currentTabPosition
    ) {
        if (isEmpty) {
            binding.invitesList.visibility = View.GONE
            binding.noInvitesTv.visibility = View.VISIBLE

            binding.noInvitesTv.text = when (selectedTabPosition) {
                0 -> "No pending invites."
                1 -> "No accepted invites."
                2 -> "No expired invites."
                else -> "No invites available."
            }
        } else {
            binding.invitesList.visibility = View.VISIBLE
            binding.noInvitesTv.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = EventInviteAdapter(emptyList(), this)
        binding.invitesList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.invitesList.adapter = adapter
    }

    private fun updateTabTitles(eventInvites: List<EventInvites>) {
        val pendingCount = eventInvites.count { it.inviteStatus == "pending" }
        val acceptedCount = eventInvites.count { it.inviteStatus == "accepted" }
        val expiredCount = eventInvites.count { it.inviteStatus == "expired" }

        binding.tabLayout.getTabAt(0)?.text = "Pending ($pendingCount)"
        binding.tabLayout.getTabAt(1)?.text = "Accepted ($acceptedCount)"
        binding.tabLayout.getTabAt(2)?.text = "Expired ($expiredCount)"
    }

    override fun onEventCardClick(invite: EventInvites) {
        lifecycleScope.launch {
            sharedViewModel.currentUserInvitedEvents.collect { events ->
                events.firstOrNull { it.eventId == invite.eventId }?.let { event ->
                    showEventDetailsBottomSheet(event)
                }
            }
        }
    }

    private fun showEventDetailsBottomSheet(event: EventData) {
        if (!isEventDetailsBottomSheetShown) {
            isEventDetailsBottomSheetShown = true
            val bottomSheetFragment = EventDetailsFragment(event)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.setOnDismissListener {
                isEventDetailsBottomSheetShown = false
            }
        }
    }

    override fun onAcceptIconClick(invite: EventInvites) {
        viewModel.updateInviteStatus(invite, sharedViewModel.currentUser.value?.userId.toString(),"accepted") { result, msg ->
            showToast(if (result) "Invite Accepted!" else msg)
        }
    }

    override fun onRejectIconClick(invite: EventInvites) {
        viewModel.updateInviteStatus(invite, sharedViewModel.currentUser.value?.userId.toString(),"rejected") { result, msg ->
            showToast(if (result) "Invite Rejected" else msg)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
