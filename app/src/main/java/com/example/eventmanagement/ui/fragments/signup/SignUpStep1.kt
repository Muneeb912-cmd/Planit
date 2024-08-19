package com.example.eventmanagement.ui.fragments.signup

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentSignUpStep1Binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.sign


class SignUpStep1 : Fragment() {
    private lateinit var binding:FragmentSignUpStep1Binding
    private val signUpViewModel:SignUpViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentSignUpStep1Binding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.adminCv.setOnClickListener{
            selectCard(binding.adminCv)
            signUpViewModel.updateUserInfo("role","Admin")
            signUpViewModel.isRoleSelected=true
        }

        binding.attendeeCv.setOnClickListener{
            selectCard(binding.attendeeCv)
            signUpViewModel.updateUserInfo("role","Attendee")
            signUpViewModel.isRoleSelected=true
        }

        //for testing purpose
        observeUser()
    }

    private fun observeUser() {
        lifecycleScope.launch {
            signUpViewModel.user.collect { user ->
                Log.d("UserData","$user")
            }
        }
    }

    private fun selectCard(selectedCard: ConstraintLayout) {
        val selectedColor =
            ContextCompat.getColor(requireContext(), R.color.md_theme_primaryFixedDim)
        val unselectedColor =
            ContextCompat.getColor(requireContext(), R.color.md_theme_surfaceContainer)

        binding.adminCv
            .setBackgroundColor(if (binding.adminCv == selectedCard) selectedColor else unselectedColor)
        binding.attendeeCv
            .setBackgroundColor(if (binding.attendeeCv == selectedCard) selectedColor else unselectedColor)
    }

}