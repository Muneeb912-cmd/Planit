package com.example.eventmanagement.ui.fragments.signup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.SignUpPagerAdapter
import com.example.eventmanagement.databinding.FragmentSignUpBinding
import com.example.eventmanagement.utils.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by activityViewModels()
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var indicators: Array<View>
    private val indicatorCount = 3
    private val args: SignUpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loginType = args.loginType
        Log.d("SignUpFragment", "LoginType: ${viewModel.loginType}")

        setupViewPager()
        setupNavigationButtons()
        setupIndicators()
    }

    private fun setupViewPager() {
        binding.stepContainer.adapter = SignUpPagerAdapter(this)

        binding.stepContainer.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateNavigationButtons(position)
            }
        })
    }

    private fun setupNavigationButtons() {
        binding.btnPrevious.setOnClickListener {
            val currentItem = binding.stepContainer.currentItem
            if (currentItem > 0) {
                binding.stepContainer.currentItem = currentItem - 1
            }
        }

        binding.btnNext.setOnClickListener {
            handleNextButtonClick()
        }

        binding.btnFinish.setOnClickListener {
            finishRegistration()
        }
    }

    private fun handleNextButtonClick() {
        when (val currentItem = binding.stepContainer.currentItem) {
            0 -> {
                if (validateCurrentStep(0)) {
                    binding.stepContainer.currentItem = currentItem + 1
                } else {
                    showValidationAlert(currentItem)
                }
            }

            1 -> {
                if (viewModel.isDataComplete) {
                    viewModel.createUserAccount()
                    observeSignUpStateOnCreateUser()
                    if (validateCurrentStep(currentItem)) {
                        binding.stepContainer.currentItem = currentItem + 1
                    } else {
                        showValidationAlert(currentItem)
                    }
                } else {
                    showValidationAlert(currentItem)
                }
            }

            2 -> finishRegistration()
            else -> null
        }
    }

    private fun validateCurrentStep(step: Int): Boolean {
        Log.d("SignUpFragment", "Validating step: $step  and ${viewModel.accountExist}")
        return when (step) {
            0 -> viewModel.isRoleSelected
            1 -> (viewModel.isDataComplete && viewModel.accountExist)
            else -> true
        }
    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, view ->
            view.background = ContextCompat.getDrawable(
                requireContext(),
                if (index == position) R.drawable.indicator_active else R.drawable.indicators_inactive
            )
        }
    }

    private fun updateNavigationButtons(position: Int) {
        binding.btnNext.visibility = if (position < indicatorCount - 1) View.VISIBLE else View.GONE
        binding.btnFinish.visibility =
            if (position == indicatorCount - 1) View.VISIBLE else View.GONE
        binding.btnPrevious.visibility = if (position > 0) View.VISIBLE else View.GONE
    }

    private fun setupIndicators() {
        val indicatorContainer = binding.indicatorContainer
        indicators = Array(indicatorCount) {
            View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.indicator_size),
                    resources.getDimensionPixelSize(R.dimen.indicator_size)
                ).apply {
                    setMargins(
                        resources.getDimensionPixelSize(R.dimen.indicator_margin),
                        0,
                        resources.getDimensionPixelSize(R.dimen.indicator_margin),
                        0
                    )
                }
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.indicators_inactive)
                indicatorContainer.addView(this)
            }
        }
    }

    private fun showValidationAlert(currentStep: Int) {
        val (title, message) = when (currentStep) {
            0 -> "Attention" to "Please select one of the given options to proceed."

            1 -> {
                val password = view?.findViewById<EditText>(R.id.password)?.text.toString()
                val confirmPassword =
                    view?.findViewById<EditText>(R.id.confirmPassword)?.text.toString()
                if (password != confirmPassword) {
                    "Password Error" to "Password and Confirm Password didn't match"
                } else if (!viewModel.isDataComplete) {
                    "Data Incomplete" to "Input field Empty, Kindly fill the input fields before continuing"
                } else {
                    null to null
                }
            }

            else -> null to null
        }

        if (title != null && message != null) {
            AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_attention)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun finishRegistration() {
        if (viewModel.isEmailVerified) {
            Toast.makeText(requireContext(), "User Registration Successful", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_signUpFragment_to_eventsMainFragment)
        } else {
            Toast.makeText(requireContext(), "Email not verified", Toast.LENGTH_SHORT).show()
        }
    }


    private fun observeSignUpStateOnCreateUser() {
        lifecycleScope.launch {
            viewModel.signUpResult.collect { result ->
                when (result) {
                    is Response.Loading -> showLoader(true)
                    is Response.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "User Successfully Created!",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (viewModel.loginType == "email_pass") {

                            viewModel.saveDataToPreferences() { isUserSavedInPrefs ->
                                if (isUserSavedInPrefs) {
                                    Toast.makeText(
                                        requireContext(),
                                        "User instance saved for seamless login",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Couldn't save user instance",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }
                        showLoader(false)
                    }

                    is Response.Error -> showErrorMessage(result.exception)
                }
            }
        }
    }

    private fun showErrorMessage(exception: Exception) {
        showLoader(false)
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage("Registration failed: ${exception.message} OR email already in use.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLoader(show: Boolean) {
        view?.findViewById<FrameLayout>(R.id.loader_overlay)?.visibility =
            if (show) View.VISIBLE else View.GONE
    }
}
