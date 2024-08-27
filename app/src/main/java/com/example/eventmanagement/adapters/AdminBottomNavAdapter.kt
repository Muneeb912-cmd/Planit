package com.example.eventmanagement.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.eventmanagement.ui.fragments.events.EventsFragment
import com.example.eventmanagement.ui.fragments.home.HomeFragment
import com.example.eventmanagement.ui.fragments.my_events.MyEventsFragment
import com.example.eventmanagement.ui.fragments.profile.ProfileFragment
import com.example.eventmanagement.ui.fragments.user_management.UserManagementFragment

class AdminBottomNavAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        HomeFragment(),
        EventsFragment(),
        UserManagementFragment(),
        ProfileFragment(),
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
