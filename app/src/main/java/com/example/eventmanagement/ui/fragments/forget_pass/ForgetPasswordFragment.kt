package com.example.eventmanagement.ui.fragments.forget_pass

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentForgetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

class ForgetPasswordFragment : Fragment() {
    private lateinit var binding:FragmentForgetPasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentForgetPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }
}