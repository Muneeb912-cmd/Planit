package com.example.eventmanagement.ui.fragments.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentSignUpStep3Binding

class SignUpStep3 : Fragment() {

    private lateinit var binding:FragmentSignUpStep3Binding
    private val signUpViewModel:SignUpViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentSignUpStep3Binding.inflate(inflater,container,false)
        binding.viewModel = signUpViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }


}