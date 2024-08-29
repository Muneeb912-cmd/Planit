package com.example.eventmanagement.ui.activities.manage_invites

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.adapters.ManageInviteAdapter
import com.example.eventmanagement.databinding.ActivityManageEventsBinding
import com.example.eventmanagement.models.User
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageEventsActivity : AppCompatActivity(), ManageInviteAdapter.OnItemClickListener {
    private lateinit var binding: ActivityManageEventsBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private val viewModel: ManageEventsViewModel by viewModels()
    private var filteredUsers = listOf<User.UserData>()
    private var currentTabPosition = 0
    private var eventId: String? = null
    private lateinit var adapter: ManageInviteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        Log.d("PassedEventID", "onCreate: $eventId")
        setupTabLayout()
        setUpAdapter()
        setupSearchListener()
        observeUsers()
        binding.tabLayout.getTabAt(0)?.select()
        binding.toolBar.setNavigationOnClickListener {
            finish()
        }
        filterUsersByInvite(0)
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentTabPosition = it.position
                    filterUsersByInvite(it.position)
                    binding.searchUser.text?.clear()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterUsersByInvite(it.position)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterUsersByInvite(it.position)
                    binding.searchUser.text?.clear()
                }
            }
        })
    }

    private fun filterUsersByInvite(selectedTabPosition: Int) {
        val allInvites = sharedViewModel.allInvites.value
        val currentEventInvites = sharedViewModel.allUsers.value.filter { user ->
            allInvites.any { invite -> invite.receiverId == user.userId && invite.eventId == eventId }
        }
        filteredUsers = when (selectedTabPosition) {
            0 -> sharedViewModel.allUsers.value.filter { user ->
                user.userId != sharedViewModel.currentUser.value?.userId.toString() && allInvites.none { invite -> invite.receiverId == user.userId && invite.eventId == eventId }
            }

            1 -> sharedViewModel.allUsers.value.filter { user ->
                user.userId != sharedViewModel.currentUser.value?.userId.toString() && allInvites.any { invite -> invite.receiverId == user.userId && invite.eventId == eventId }
            }

            else -> sharedViewModel.allUsers.value
        }
        adapter.updatedUsersList(filteredUsers)
        adapter.updatedCurrentEventInvites(currentEventInvites)
    }

    private fun setupSearchListener() {
        binding.searchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchEvents()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun setUpAdapter() {
        adapter = ManageInviteAdapter(emptyList(), emptyList(), this)
        binding.usersList.layoutManager = LinearLayoutManager(this)
        binding.usersList.adapter = adapter
    }

    private fun observeUsers() {
        lifecycleScope.launch {
            launch {
                sharedViewModel.allUsers.collect {
                    filterUsersByInvite(currentTabPosition)
                }
            }
            launch {
                sharedViewModel.allInvites.collect {
                    filterUsersByInvite(currentTabPosition)
                }
            }
        }
    }

    private fun searchEvents() {
        val query = binding.searchUser.text.toString().trim().lowercase()
        filteredUsers = if (query.isEmpty()) {
            filterUsersByInvite(currentTabPosition)
            filteredUsers
        } else {
            filteredUsers.filter {
                it.userName?.lowercase()?.contains(query) == true ||
                        it.userEmail?.lowercase()?.contains(query) == true
            }
        }
        adapter.updatedUsersList(filteredUsers)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSendInviteButtonClick(userData: User.UserData, key: String) {
        if (key == "send") {
            viewModel.createInvite(
                eventId.toString(),
                sharedViewModel.currentUser.value?.userId.toString(),
                userData
            ) { result ->
                if (result) {
                    Toast.makeText(this, "Invite Sent To ${userData.userName}!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Couldn't Send Invite To${userData.userName}!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            viewModel.deleteInvite(
                eventId.toString(),
                userData.userId.toString(),
            ) { result ->
                if (result) {
                    Toast.makeText(this, "Invite UnSent!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Couldn't Un-Send Invite!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

//    private fun observeStates() {
//        lifecycleScope.launch {
//            viewModel.states.collect { response ->
//                when (response) {
//                    is Response.Loading -> {
//
//                    }
//
//                    is Response.Error -> {
//
//                    }
//
//                    is Response.Success -> {
//
//                    }
//                }
//            }
//        }
//    }

    companion object {
        const val EXTRA_EVENT_ID = "com.example.eventmanagement.EXTRA_EVENT_ID"
    }
}