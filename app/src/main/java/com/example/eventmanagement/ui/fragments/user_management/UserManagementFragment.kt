package com.example.eventmanagement.ui.fragments.user_management

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventmanagement.adapters.UserDataAdapter
import com.example.eventmanagement.databinding.FragmentUserManagementBinding
import com.example.eventmanagement.models.User
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile.EditProfileFragment
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserManagementFragment : Fragment(), UserDataAdapter.OnItemClickListener {

    private lateinit var binding: FragmentUserManagementBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var isEditProfileBottomSheetShown = false
    private var currentTabPosition = 0
    private var filteredUsers = listOf<User.UserData>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        binding.tabLayout.getTabAt(0)?.select()
        setupTabLayout()
        observeUsers()
        filterUsers(0)
        setupSearchListener()
    }


    private fun setupSearchListener() {
        binding.searchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentTabPosition = it.position
                    filterUsers(it.position)
                    binding.searchUser.text?.clear()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterUsers(it.position)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.let {
                    filterUsers(it.position)
                    binding.searchUser.text?.clear()
                }
            }
        })
    }


    private fun filterUsers(selectedTabPosition: Int) {
        filteredUsers = when (selectedTabPosition) {
            0 -> sharedViewModel.allUsers.value.filter { it.userBanned == false && it.userId != sharedViewModel.currentUser.value?.userId.toString() }
            1 -> sharedViewModel.allUsers.value.filter { it.userBanned == true && it.userId != sharedViewModel.currentUser.value?.userId.toString() }
            else -> sharedViewModel.allUsers.value
        }
        Log.d("Users", "filterEvents: ${sharedViewModel.allUsers.value}")
        (binding.userList.adapter as? UserDataAdapter)?.updatedUsersList(
            filteredUsers
        )
    }

    private fun observeUsers() {
        lifecycleScope.launch {
            sharedViewModel.allUsers.collect { users ->
                filterUsers(currentTabPosition)
                updateTabTitles(users)
            }
        }
    }

    private fun updateTabTitles(users: List<User.UserData>) {
        val userCount =
            users.count { it.userBanned == false && it.userId != sharedViewModel.currentUser.value?.userId.toString() }
        val bandedUsersCount =
            users.count { it.userBanned == true && it.userId != sharedViewModel.currentUser.value?.userId.toString() }

        binding.tabLayout.getTabAt(0)?.text = "Users ($userCount)"
        binding.tabLayout.getTabAt(1)?.text = "Suspended Users ($bandedUsersCount)"
    }

    private fun setUpAdapter() {
        val adapter = UserDataAdapter(emptyList(), this)
        binding.userList.layoutManager = LinearLayoutManager(context)
        binding.userList.adapter = adapter
    }

    override fun onItemClick(userData: User.UserData) {
        if (!isEditProfileBottomSheetShown) {
            isEditProfileBottomSheetShown = true

            val bottomSheetFragment = EditProfileFragment("Modify", userData)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.setOnDismissListener {
                isEditProfileBottomSheetShown = false
            }
        }
    }

    private fun searchUsers() {
        val query = binding.searchUser.text.toString().trim().lowercase()
        filteredUsers = if (query.isEmpty()) {
            filterUsers(currentTabPosition)
            filteredUsers
        } else {
            filteredUsers.filter {
                it.userName?.lowercase()?.contains(query) == true ||
                        it.userEmail?.lowercase()?.contains(query) == true
            }
        }
        (binding.userList.adapter as? UserDataAdapter)?.updatedUsersList(
            filteredUsers
        )
    }
}