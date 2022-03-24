package com.spyneai.shoot.ui.dialogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.sdk.Spyne
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
            if (Utilities.getBool(requireContext(),AppConstants.FROM_SDK,false)){
                val intent = Spyne.intent
                intent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }else
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