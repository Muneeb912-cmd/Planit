package com.example.eventmanagement.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.eventmanagement.ui.fragments.events.EventsFragment
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.fav_evetns.FavEventsFragment
import com.example.eventmanagement.ui.fragments.home.HomeFragment
import com.example.eventmanagement.ui.fragments.my_events.MyEventsFragment
import com.example.eventmanagement.ui.fragments.profile.ProfileFragment

class BottomNavAdapter (activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        HomeFragment(),
        EventsFragment(),
        MyEventsFragment(),
        ProfileFragment(),
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}