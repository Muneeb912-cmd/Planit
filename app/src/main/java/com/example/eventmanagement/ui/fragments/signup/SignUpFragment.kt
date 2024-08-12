package com.example.eventmanagement.ui.fragments.signup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.eventmanagement.R
import com.example.eventmanagement.adapters.SignUpPagerAdapter
import com.example.eventmanagement.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel: SignUpViewModel by activityViewModels()
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var indicators: Array<View>
    private val indicatorCount = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupNavigationButtons()
        setupIndicators()
    }

    private fun setupViewPager() {
        val pagerAdapter = SignUpPagerAdapter(this)
        binding.stepContainer.adapter = pagerAdapter

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
            val currentItem = binding.stepContainer.currentItem
            if (validateCurrentStep(currentItem)) {
                if (currentItem < indicatorCount - 1) {
                    binding.stepContainer.currentItem = currentItem + 1
                } else {
                    finishRegistration()
                }
            } else {
                showValidationAlert(currentItem)
            }
        }

        binding.btnFinish.setOnClickListener {
            finishRegistration()
        }
    }

    private fun validateCurrentStep(step: Int): Boolean {
        Log.d("Role Selected", "validateCurrentStep: ${viewModel.isRoleSelected}")
        return when (step) {
            0 -> viewModel.isRoleSelected
            1 -> viewModel.isDataComplete
            else -> true
        }
    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, view ->
            view.background = if (index == position) {
                ContextCompat.getDrawable(requireContext(), R.drawable.indicator_active)
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.indicators_inactive)
            }
        }
    }

    private fun updateNavigationButtons(position: Int) {
        when (position) {
            2 -> {
                binding.btnNext.visibility = View.GONE
                binding.btnFinish.visibility = View.VISIBLE
            }

            0 -> {
                binding.btnNext.visibility = View.VISIBLE
                binding.btnFinish.visibility = View.GONE
            }

            else -> {
                binding.btnNext.visibility = View.VISIBLE
                binding.btnPrevious.visibility = View.VISIBLE
                binding.btnFinish.visibility = View.GONE
            }
        }
    }

    private fun setupIndicators() {
        val indicatorContainer = binding.indicatorContainer
        indicators = Array(indicatorCount) { index ->
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
        if (currentStep == 0) {
            AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_attention)
                .setTitle("Attention")
                .setMessage("Please select one of the given option to proceed")
                .setPositiveButton("OK", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_attention)
                .setTitle("Attention")
                .setMessage("Please completely fill the required information.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun finishRegistration() {
        // Handle the registration completion logic here
        // For example, you might navigate to a different screen or show a success message
    }
}
