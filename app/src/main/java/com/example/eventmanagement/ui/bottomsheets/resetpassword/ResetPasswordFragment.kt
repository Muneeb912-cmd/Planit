package com.example.eventmanagement.ui.bottomsheets.resetpassword

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.eventmanagement.databinding.FragmentResetPasswordBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentResetPasswordBinding
    private val viewModel: ResetPasswordViewModel by viewModels()
    private var onDismissListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            sendRestPassEmail.setOnClickListener {
                val email = email.text.toString()
                if (email != "") {
                    Log.d("userEmail", "onViewCreated: $email")
                    viewModel.sendPasswordRestEmail(email) { emailSent ->
                        if (emailSent) {
                            notifyMsg.text = "Password Reset Email Sent!"
                            notifyMsg.visibility = View.VISIBLE
                        } else {
                            notifyMsg.text = "Error Sending Password Reset Email!"
                            notifyMsg.visibility = View.VISIBLE
                        }
                    }
                } else {
                    notifyMsg.text = "Email Input Field Empty"
                    notifyMsg.visibility = View.VISIBLE
                }
            }
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
        }
    }
    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

}