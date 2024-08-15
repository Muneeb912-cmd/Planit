package com.example.eventmanagement.ui.fragments.events_main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentEventsMainBinding
import com.example.eventmanagement.adapters.BottomNavAdapter

class EventsMainFragment : Fragment() {

    private lateinit var binding: FragmentEventsMainBinding
    private lateinit var viewPagerAdapter: BottomNavAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerAdapter = BottomNavAdapter(requireActivity())
        binding.viewPager.adapter = viewPagerAdapter

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> binding.viewPager.currentItem = 0
                R.id.events -> binding.viewPager.currentItem = 1
                R.id.myEvents -> binding.viewPager.currentItem = 2
                R.id.profile-> binding.viewPager.currentItem = 3
                else -> false
            }
            true
        }

        binding.bottomNavView.selectedItemId = R.id.bottomNavView

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavView.menu.getItem(position).isChecked = true
            }
        })
    }
}
