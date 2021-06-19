package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.gotoHome
import com.spyneai.shoot.data.ShootViewModel

class ShootExitDialog : BaseDialogFragment<ShootViewModel, DialogExitBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            requireContext().gotoHome()
            dismiss()

        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogExitBinding.inflate(inflater, container, false)
}