package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.DialogExitBinding
import com.spyneai.databinding.DialogSkipBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel

class SkipShootDialog : BaseDialogFragment<ShootViewModel, DialogSkipBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {

            when (viewModel.categoryDetails.value?.imageType) {
                "Interior" -> {
                    if (viewModel.interiorShootNumber.value == viewModel.interiorAngles.value?.minus(
                            1
                        )
                    ) {
                        viewModel.checkMiscShootStatus(getString(R.string.app_name))
                    } else {
                        viewModel.interiorShootNumber.value =
                            viewModel.interiorShootNumber.value!! + 1
                    }
                }

                "Focus Shoot" -> {
                    if (viewModel.miscShootNumber.value == viewModel.miscAngles.value?.minus(1)) {
                        viewModel.selectBackground(getString(R.string.app_name))
                    } else {
                        viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
                    }
                }
            }

            dismiss()
        }
    }




    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogSkipBinding.inflate(inflater, container, false)
}