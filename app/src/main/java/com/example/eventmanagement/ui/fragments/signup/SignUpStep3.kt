package com.example.eventmanagement.ui.fragments.signup

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.eventmanagement.databinding.FragmentSignUpStep3Binding
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpStep3 : Fragment() {

    private lateinit var binding: FragmentSignUpStep3Binding
    private val signUpViewModel: SignUpViewModel by activityViewModels()
    private var emailVerificationJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpStep3Binding.inflate(inflater, container, false)
        binding.viewModel = signUpViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeUI()
    }

    @SuppressLint("SetTextI18n")
    private fun initializeUI() {
        binding.msgTitle.text = "User Email Not Verified"
        binding.msg.text = "Click the above button to get verification email."
        binding.retryButton.visibility = View.VISIBLE
        binding.timerTextView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        binding.retryButton.setOnClickListener {
            if (binding.retryButton.isEnabled) {
                sendVerificationEmail()
            }
        }
    }

    private fun sendVerificationEmail() {
        binding.retryButton.isEnabled = false
        signUpViewModel.sendVerificationEmail()
        signUpViewModel.checkVerificationEmail()
        observeEmailVerificationStatus()
        startVerificationDelay()
        showLoadingState()
    }

    @SuppressLint("SetTextI18n")
    private fun startVerificationDelay() {
        binding.timerTextView.visibility = View.VISIBLE
        lifecycleScope.launch {
            var timeLeft = 60
            while (timeLeft > 0) {
                binding.timerTextView.text = "Retry available in $timeLeft seconds"
                delay(1000)
                timeLeft--
            }
            if (!signUpViewModel.isEmailVerified) {
                showErrorState()
                binding.timerTextView.visibility=View.GONE
            }
        }
    }

    private fun observeEmailVerificationStatus() {
        emailVerificationJob = lifecycleScope.launch {
            while (!signUpViewModel.isEmailVerified) {
                signUpViewModel.checkVerificationEmail()
                delay(2000)
            }
            signUpViewModel.emailVerificationStatus.collect { result ->
                when (result) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        if (signUpViewModel.isEmailVerified) {
                            showSuccessState()
                        }
                    }
                    is Response.Error -> {}
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun showLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.msgTitle.text = "Checking Verification Status..."
        binding.msg.text = "Please wait while we check if your email is verified."
        binding.retryButton.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun showSuccessState() {
        binding.progressBar.visibility = View.GONE
        binding.msgTitle.text = "User Email Verified"
        binding.msg.text = "Your email is verified. You can now proceed."
        binding.retryButton.visibility = View.GONE
        binding.timerTextView.visibility = View.GONE
        signUpViewModel.isEmailVerified=true
    }

    @SuppressLint("SetTextI18n")
    private fun showErrorState() {
        binding.progressBar.visibility = View.GONE
        binding.msgTitle.text = "Verify Your Email"
        binding.msg.text = "Failed to verify email. Please try again."
        binding.retryButton.visibility = View.VISIBLE
        binding.retryButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emailVerificationJob?.cancel()
    }
}
