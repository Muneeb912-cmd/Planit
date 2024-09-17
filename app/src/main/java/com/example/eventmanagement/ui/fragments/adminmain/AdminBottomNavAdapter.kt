package com.example.eventmanagement.ui.fragments.adminmain

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.eventmanagement.ui.fragments.adminhome.AdminHomeFragment
import com.example.eventmanagement.ui.fragments.manageevents.EventManagementFragment
import com.example.eventmanagement.ui.fragments.profile.ProfileFragment
import com.example.eventmanagement.ui.fragments.usermanagement.UserManagementFragment

class AdminBottomNavAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        AdminHomeFragment(),
        EventManagementFragment(),
        UserManagementFragment(),
        ProfileFragment(),
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
