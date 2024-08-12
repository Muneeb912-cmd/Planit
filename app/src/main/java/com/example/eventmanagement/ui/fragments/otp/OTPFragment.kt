package com.example.eventmanagement.ui.fragments.otp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentOTPBinding


class OTPFragment : Fragment() {
    private lateinit var binding:FragmentOTPBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentOTPBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.verifyBtn.setOnClickListener{
            findNavController().navigate(R.id.action_OTPFragment_to_eventsMainFragment)
        }
    }
}