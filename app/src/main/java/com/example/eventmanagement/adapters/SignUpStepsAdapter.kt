package com.example.eventmanagement.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.eventmanagement.ui.fragments.signup.SignUpStep1
import com.example.eventmanagement.ui.fragments.signup.SignUpStep2
import com.example.eventmanagement.ui.fragments.signup.SignUpStep3

class SignUpPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3 // Number of fragments

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SignUpStep1()
            1 -> SignUpStep2()
            2 -> SignUpStep3()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}