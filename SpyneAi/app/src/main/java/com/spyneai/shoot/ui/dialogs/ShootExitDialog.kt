package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.gotoHome
import com.spyneai.setLocale
import com.spyneai.shoot.data.ShootViewModel

class ShootExitDialog : BaseDialogFragment<ShootViewModel, DialogExitBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshText()

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {
            requireContext().gotoHome()
            dismiss()

        }
    }


    fun refreshText(){
        requireContext().setLocale()
        binding.tvSkuNameDialog.text=getString(R.string.exits)
        binding.btnNo.text=getString(R.string.no)
        binding.btnYes.text=getString(R.string.yes)

    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogExitBinding.inflate(inflater, container, false)
}