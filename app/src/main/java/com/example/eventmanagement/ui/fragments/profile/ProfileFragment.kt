package com.example.eventmanagement.ui.fragments.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.BottomNavAdapter
import com.example.eventmanagement.databinding.FragmentProfileBinding
import com.example.eventmanagement.di.CitiesCountries
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile.EditProfileFragment
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.reset_password.ResetPasswordFragment
import com.example.eventmanagement.ui.fragments.login.LoginFragment
import com.example.eventmanagement.ui.fragments.login.LoginViewModel
import com.example.eventmanagement.utils.Response
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    @CitiesCountries
    lateinit var citiesCountries: ArrayList<String>
    private lateinit var viewPagerAdapter: BottomNavAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            notificationToggle.setOnCheckedChangeListener { _, isChecked ->
                handleNotificationToggle(isChecked)
            }

            profileToggle.setOnCheckedChangeListener { _, isChecked ->
                handleProfileToggle(isChecked)
            }

            userLocationTv.setOnClickListener {
                setUserLocation()
            }

            changePassTv.setOnClickListener {
                openPasswordReset()
            }

            logoutBtn.setOnClickListener {
                showLogOutDialog()
            }

            userProfileCard.setOnClickListener {
                openEditProfile()
            }
        }
    }

    private fun observeSignOutState() {
        lifecycleScope.launchWhenStarted {
            viewModel.profileStates.collect { state ->
                when (state) {
                    is Response.Loading -> {
                        showLoader(true)
                    }
                    is Response.Success -> {
                        findNavController().navigate(
                            R.id.action_eventsMainFragment_to_loginFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph_xml, true)
                                .setLaunchSingleTop(true)
                                .build()
                        )
                        showLoader(false)
                    }
                    is Response.Error -> {
                        Toast.makeText(context, state.exception.message, Toast.LENGTH_SHORT).show()
                        showLoader(false)
                    }
                }
            }
        }
    }


    private fun openEditProfile() {
        val bottomSheetFragment = EditProfileFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }

    private fun openPasswordReset() {
        val bottomSheetFragment = ResetPasswordFragment()
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }


    private fun setUserLocation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Your Location")
            .setIcon(R.drawable.ic_location)
            .setItems(citiesCountries.toTypedArray()) { _, which ->
                val selectedLocation = citiesCountries[which]
                binding.userLocationTv.text = selectedLocation
                Toast.makeText(
                    requireContext(),
                    "Location set to $selectedLocation",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleNotificationToggle(isChecked: Boolean) {
        if (isChecked) {
            Toast.makeText(requireContext(), "Notification Turned On", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Notification Turned Off", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleProfileToggle(isChecked: Boolean) {
        if (isChecked) {
            Toast.makeText(requireContext(), "Profile set to Public", Toast.LENGTH_SHORT).show()
            binding.profileStatus.text = "Public"
        } else {
            Toast.makeText(requireContext(), "Profile set to Private", Toast.LENGTH_SHORT).show()
            binding.profileStatus.text = "Private"
        }
    }

    private fun showLogOutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you want to LogOut ?")
        builder.setTitle("Logout ?")
        builder.setIcon(R.drawable.ic_log_out)
        builder.setCancelable(false)
        builder.setPositiveButton("Confirm") { _, _ ->
            userLogOut()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun userLogOut() {
        viewModel.signOut()
        observeSignOutState()
    }

    private fun showLoader(show: Boolean) {
        view?.findViewById<FrameLayout>(R.id.loader_overlay)?.visibility = if (show) View.VISIBLE else View.GONE
    }
}