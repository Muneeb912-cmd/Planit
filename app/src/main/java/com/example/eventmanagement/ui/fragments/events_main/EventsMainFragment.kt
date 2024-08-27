package com.example.eventmanagement.ui.fragments.events_main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.BottomNavAdapter
import com.example.eventmanagement.databinding.FragmentEventsMainBinding
import com.example.eventmanagement.ui.activities.event_invites.EventInviteActivity
import com.example.eventmanagement.ui.activities.fav_events.FavEventActivity
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import kotlinx.coroutines.launch

class EventsMainFragment : Fragment() {

    private lateinit var binding: FragmentEventsMainBinding
    private lateinit var viewPagerAdapter: BottomNavAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsMainBinding.inflate(inflater, container, false)
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateInviteNotifier()
        viewPagerAdapter = BottomNavAdapter(requireActivity())
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> binding.viewPager.currentItem = 0
                R.id.events -> binding.viewPager.currentItem = 1
                R.id.myEvents -> binding.viewPager.currentItem = 2
                R.id.profile -> binding.viewPager.currentItem = 3
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

        binding.favEvents.setOnClickListener {
            val intent = Intent(requireContext(), FavEventActivity::class.java)
            startActivity(intent)
        }

        binding.eventInvites.setOnClickListener{
            val intent = Intent(requireContext(), EventInviteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateInviteNotifier(){
        lifecycleScope.launch {
            sharedViewModel.eventInvites.collect{events->
                val pendingEvents=events.filter { it.inviteStatus=="pending" }
                if(pendingEvents.isNotEmpty()){
                    binding.eventInvitesNotifier.visibility=View.VISIBLE
                }else{
                    binding.eventInvitesNotifier.visibility=View.GONE
                }
            }
        }
    }
}
