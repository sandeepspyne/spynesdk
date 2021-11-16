package com.spyneai.shoot.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogSkipBinding
import com.spyneai.shoot.data.ShootViewModel

class SkipShootDialog : BaseDialogFragment<ShootViewModel, DialogSkipBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {

//            when (viewModel.categoryDetails.value?.imageType) {
//                "Interior" -> {
//                    if (viewModel.interiorShootNumber.value == viewModel.interiorAngles.value?.minus(
//                            1
//                        )
//                    ) {
//                        viewModel.checkMiscShootStatus(getString(R.string.app_name))
//                    } else {
//                        viewModel.interiorShootNumber.value =
//                            viewModel.interiorShootNumber.value!! + 1
//                    }
//                }
//
//                "Focus Shoot" -> {
//                    if (viewModel.miscShootNumber.value == viewModel.miscAngles.value?.minus(1)) {
//                        viewModel.selectBackground(getString(R.string.app_name))
//                    } else {
//                        viewModel.miscShootNumber.value = viewModel.miscShootNumber.value!! + 1
//                    }
//                }
//            }

            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()
                ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogSkipBinding.inflate(inflater, container, false)
}