package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.DialogInteriorHintBinding
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.utils.shoot

class InteriorHintDialog : BaseDialogFragment<ShootViewModel, DialogInteriorHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.tvSkip.setOnClickListener {
            viewModel.showMiscDialog.value = true
            viewModel.iniProgressFrame.value = false
            viewModel.startInteriorShots.value = true
          checkMiscShootStatus()
            dismiss()
        }

        binding.tvShootNowInterior.setOnClickListener {
            viewModel.iniProgressFrame.value = false
            viewModel.startInteriorShots.value = true
            dismiss()
        }
    }

    private fun checkMiscShootStatus() {
        viewModel.subCategoriesResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    when {
                        it.value.miscellaneous.isNotEmpty() -> {
                            viewModel.showMiscDialog.value = true
                        }
                        else -> {
                            selectBackground()
                        }
                    }
                }
                else -> { }
            }
        })

    }

    private fun selectBackground() {
        if(getString(R.string.app_name) == AppConstants.OLA_CABS)
            viewModel.show360InteriorDialog.value = true
        else
            viewModel.selectBackground.value = true
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogInteriorHintBinding.inflate(inflater, container, false)
}