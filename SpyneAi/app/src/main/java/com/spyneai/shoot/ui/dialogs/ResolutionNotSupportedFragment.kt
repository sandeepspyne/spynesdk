package com.spyneai.shoot.ui.dialogs

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.spyneai.R
import com.spyneai.databinding.DialogResolutionNotSupportedBinding
import com.spyneai.databinding.DialogTopUpBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class ResolutionNotSupportedFragment : DialogFragment() {

    private var _binding : DialogResolutionNotSupportedBinding? = null
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DialogResolutionNotSupportedBinding.inflate(inflater, container, false)

        isCancelable = false

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.ivClose?.setOnClickListener {
            dismiss()
        }

        binding?.tvOKay?.setOnClickListener {
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        if (dialog != null){
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}