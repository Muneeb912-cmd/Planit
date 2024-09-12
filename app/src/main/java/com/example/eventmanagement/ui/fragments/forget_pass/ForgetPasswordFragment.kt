package com.example.eventmanagement.ui.fragments.forget_pass

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.eventmanagement.databinding.FragmentForgetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgetPasswordFragment : Fragment() {
    private lateinit var binding:FragmentForgetPasswordBinding
    private val viewModel:ForgotPassViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentForgetPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            sendRestPassEmail.setOnClickListener{
                val email=email.text.toString()
                if (email!=""){
                    Log.d("userEmail", "onViewCreated: $email")
                    viewModel.sendPasswordRestEmail(email){emailSent->
                        if(emailSent){
                            emailSentTv.visibility=View.VISIBLE
                        }else{
                            Toast.makeText(requireContext(),"Password reset email not sent",Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(requireContext(),"Email input field empty!",Toast.LENGTH_SHORT).show()
                }
            }
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
}