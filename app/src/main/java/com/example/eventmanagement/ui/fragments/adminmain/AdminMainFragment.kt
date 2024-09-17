package com.example.eventmanagement.ui.fragments.adminmain

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentAdminMainBinding
import com.example.eventmanagement.di.StaticDataModule
import com.example.eventmanagement.ui.fragments.profile.ProfileViewModel
import com.example.eventmanagement.ui.sharedviewmodel.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminMainFragment : Fragment() {
    private lateinit var binding:FragmentAdminMainBinding
    private lateinit var viewPagerAdapter: AdminBottomNavAdapter
    private val sharedViewModel:SharedViewModel by activityViewModels()
    private val citiesCountries = StaticDataModule.provideCitiesCountries()
    private val viewModel: AdminMainViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentAdminMainBinding.inflate(inflater,container,false)
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPagerAdapter = AdminBottomNavAdapter(requireActivity())
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> binding.viewPager.currentItem = 0
                R.id.events -> binding.viewPager.currentItem = 1
                R.id.myEvents -> binding.viewPager.currentItem = 2
                R.id.profile -> binding.viewPager.currentItem = 3
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

        binding.userLocationTv.setOnClickListener {
            setUserLocation()
        }
    }

    private fun setUserLocation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Your Location")
            .setIcon(R.drawable.ic_location)
            .setItems(citiesCountries.toTypedArray()) { _, which ->
                val selectedLocation = citiesCountries[which]
                viewModel.updateUserLocation(
                    sharedViewModel.currentUser.value?.userId.toString(),
                    selectedLocation
                ) { result ->
                    if (result) {
                        Toast.makeText(
                            requireContext(),
                            "User location changed to: $selectedLocation",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: Couldn't change user location!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}