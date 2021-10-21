package com.spyneai.shoot.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.spyneai.base.BaseDialogFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.databinding.DialogNoMagnatoMeterBinding
import com.spyneai.databinding.DialogSkipBinding
import com.spyneai.shoot.data.ShootViewModel

class NoMagnaotoMeterDialog : BaseDialogFragment<DashboardViewModel, DialogNoMagnatoMeterBinding>() {

//    private var _binding : DialogNoMagnatoMeterBinding? = null
//    private val binding get() = _binding
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        _binding = DialogNoMagnatoMeterBinding.inflate(inflater, container, false)
//
//
//
//        return binding?.root
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        binding?.tvOKay?.setOnClickListener {
            viewModel.continueAnyway.value = true
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        if (dialog != null){
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogNoMagnatoMeterBinding.inflate(inflater, container, false)
}