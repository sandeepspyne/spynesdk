package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogReclickBinding
import com.spyneai.shoot.data.ShootViewModel

class ReclickDialog : BaseDialogFragment<ShootViewModel, DialogReclickBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnClickListener {

            arguments?.let {
                if (it.getString("image_type").toString().contains("Info")) {
                    viewModel.categoryDetails.value?.imageType = it.getString("image_type").toString()
                    viewModel.currentShoot = it.getInt("position")
                    viewModel.hideLeveler.value = true
                    viewModel.overlayId = it.getInt("overlay_id")
                    viewModel.updateSelectItem.value = true

                } else {
                    viewModel.showLeveler.value = true
                    viewModel.categoryDetails.value?.imageType = it.getString("image_type").toString()
                    viewModel.currentShoot = it.getInt("position")
                    viewModel.overlayId = it.getInt("overlay_id")
                    viewModel.updateSelectItem.value = true
                }

            }
            dismiss()
        }
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogReclickBinding.inflate(inflater, container, false)
}