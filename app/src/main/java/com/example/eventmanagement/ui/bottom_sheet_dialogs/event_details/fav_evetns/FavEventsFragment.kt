package com.example.eventmanagement.ui.bottom_sheet_dialogs.event_details.fav_evetns

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.eventmanagement.databinding.FragmentFavEventsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FavEventsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFavEventsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            closeBottomSheet.setOnClickListener {
                dismiss()
            }
        }
    }
}