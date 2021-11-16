package com.spyneai.dashboard.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.spyneai.databinding.DialogOutdatedAppVersionBinding

class OutdatedVersionDialog : DialogFragment() {

    private var _binding : DialogOutdatedAppVersionBinding? = null
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DialogOutdatedAppVersionBinding.inflate(inflater, container, false)

        isCancelable = false

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.ivClose?.setOnClickListener {
            requireActivity().finish()
        }

        binding?.tvOKay?.setOnClickListener {
            requireActivity().finish()
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