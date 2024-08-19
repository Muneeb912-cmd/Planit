package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.reset_password

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eventmanagement.R
import com.example.eventmanagement.databinding.FragmentForgetPasswordBinding
import com.example.eventmanagement.databinding.FragmentResetPasswordBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ResetPasswordFragment : BottomSheetDialogFragment() {
    private lateinit var binding:FragmentResetPasswordBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentResetPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            sendRestPassEmail.setOnClickListener{

            }
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
        }
    }

}