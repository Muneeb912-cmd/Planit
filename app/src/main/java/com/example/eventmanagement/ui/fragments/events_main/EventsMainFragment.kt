package com.example.eventmanagement.ui.fragments.events_main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.BottomNavAdapter
import com.example.eventmanagement.databinding.FragmentEventsMainBinding
import com.example.eventmanagement.ui.activities.event_invites.EventInviteActivity
import com.example.eventmanagement.ui.activities.fav_events.FavEventActivity
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EventsMainFragment : Fragment() {

    private lateinit var binding: FragmentEventsMainBinding
    private lateinit var viewPagerAdapter: BottomNavAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val viewModel:EventMainViewModel by viewModels()

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
        observeUser()
    }

    private fun observeUser(){
        lifecycleScope.launch {
            sharedViewModel.currentUser.collect{userData->
                if(userData?.isUserBanned == true){
                    showAlertDialog("Attention","Your account is suspended by admin. Kindly contact with admin for more information at:\n\n dukocommunity@gmail.com",R.drawable.ic_attention)
                }
            }
        }
    }
    private fun updateInviteNotifier(){
        lifecycleScope.launch {
            sharedViewModel.currentUserInvites.collect{events->
                val pendingEvents=events.filter { it.inviteStatus=="pending" }
                if(pendingEvents.isNotEmpty()){
                    binding.eventInvitesNotifier.visibility=View.VISIBLE
                }else{
                    binding.eventInvitesNotifier.visibility=View.GONE
                }
            }
        }
    }

    private fun showAlertDialog(title: String, msg: String, icon: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setIcon(icon)
            .setPositiveButton("OK") { dialog, _ ->
                signOutUser()
            }
            .show()
    }

    private fun signOutUser() {
        viewModel.signOut(){result,msg->
            if(result){
                findNavController().navigate(
                    R.id.action_eventsMainFragment_to_loginFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph_xml, true)
                        .setLaunchSingleTop(true)
                        .build()
                )
                sharedViewModel.resetViewModel()
            }else{
                Log.d("Exception", "signOutUser: $msg")
            }
        }
    }
}
