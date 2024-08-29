package com.example.eventmanagement.ui.fragments.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentProfileBinding
import com.example.eventmanagement.di.CitiesCountries
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.ediit_profile.EditProfileFragment
import com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.reset_password.ResetPasswordFragment
import com.example.eventmanagement.ui.shared_view_model.SharedViewModel
import com.example.eventmanagement.utils.Response
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var isEditProfileBottomSheetShown = false

    @Inject
    @CitiesCountries
    lateinit var citiesCountries: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeListeners()
        Glide.with(requireContext())
            .load(sharedViewModel.currentUser.value?.userImg)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
            )
            .into(binding.userProfileImg)

        binding.notificationToggle.isChecked =
            sharedViewModel.currentUser.value?.isNotificationsAllowed == true
        binding.profileToggle.isChecked =
            sharedViewModel.currentUser.value?.isProfilePrivate == true

        if (sharedViewModel.currentUser.value?.userLoginType == "google") binding.passwordRestLayout.visibility =
            View.GONE

    }

    private fun initializeListeners() {
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
                        lifecycleScope.launch {
                            if (sharedViewModel.currentUser.value?.userRole == "Attendee") {
                                findNavController().navigate(
                                    R.id.action_eventsMainFragment_to_loginFragment,
                                    null,
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.nav_graph_xml, true)
                                        .setLaunchSingleTop(true)
                                        .build()
                                )
                                sharedViewModel.resetViewModel()
                                showLoader(false)
                            } else {
                                findNavController().navigate(
                                    R.id.action_adminMainFragment_to_loginFragment,
                                    null,
                                    NavOptions.Builder()
                                        .setPopUpTo(R.id.nav_graph_xml, true)
                                        .setLaunchSingleTop(true)
                                        .build()
                                )
                                sharedViewModel.resetViewModel()
                                showLoader(false)
                            }

                        }
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
        if (!isEditProfileBottomSheetShown) {
            isEditProfileBottomSheetShown = true

            val bottomSheetFragment = EditProfileFragment("Update", null)
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.setOnDismissListener {
                isEditProfileBottomSheetShown = false
            }
        }
    }

    private fun openPasswordReset() {
        if (!isEditProfileBottomSheetShown) {
            isEditProfileBottomSheetShown = true
            val bottomSheetFragment = ResetPasswordFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.setOnDismissListener {
                isEditProfileBottomSheetShown = false
            }
        }

    }


    private fun setUserLocation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Select Your Location")
            .setIcon(R.drawable.ic_location)
            .setItems(citiesCountries.toTypedArray()) { _, which ->
                val selectedLocation = citiesCountries[which]
                viewModel.updateUserLocation(
                    sharedViewModel.currentUser.value?.userId.toString(),
                    selectedLocation
                ) { result ->
                    if (result) {
                        binding.userLocationTv.text = selectedLocation
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

    private fun handleNotificationToggle(isChecked: Boolean) {
        viewModel.updateUserNotificationStatus(
            sharedViewModel.currentUser.value?.userId.toString(),
            isChecked
        ) { result ->
            if (result) {
                if (isChecked) {
                    Toast.makeText(requireContext(), "Notification Turned On", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Notification Turned Off", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error: Couldn't update user notification settings",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleProfileToggle(isChecked: Boolean) {
        viewModel.updateUserProfileStatus(
            sharedViewModel.currentUser.value?.userId.toString(),
            isChecked
        ) { result ->
            if (result) {
                if (isChecked) {
                    Toast.makeText(requireContext(), "Profile set to Public", Toast.LENGTH_SHORT)
                        .show()
                    binding.profileStatus.text = "Public"
                } else {
                    Toast.makeText(requireContext(), "Profile set to Private", Toast.LENGTH_SHORT)
                        .show()
                    binding.profileStatus.text = "Private"
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error: Couldn't update user profile status",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showLogOutDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setMessage("Do you want to Log Out?")
            .setTitle("Logout")
            .setIcon(R.drawable.ic_log_out)
            .setCancelable(false)
            .setPositiveButton("Confirm") { _, _ ->
                Log.d(
                    "userRole",
                    "showLogOutDialog: ${sharedViewModel.currentUser.value?.userRole}"
                )
                userLogOut()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
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
        view?.findViewById<FrameLayout>(R.id.loader_overlay)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }
}