package com.spyneai

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.FragmentInfoDialogBinding
import com.spyneai.shoot.data.ShootViewModel


class InfoDialog :  BaseDialogFragment<ShootViewModel, FragmentInfoDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEndProject.setOnClickListener {
            if (viewModel.fromDrafts){
                viewModel.stopShoot.value = true
            }else {
                if (viewModel.isStopCaptureClickable)
                    viewModel.stopShoot.value = true
            }

            dismiss()
        }

        binding.llShootInfo.setOnClickListener {
//            viewModel.showGrid.value = viewModel.getCameraSetting().isGridActive
//            viewModel.showLeveler.value = viewModel.getCameraSetting().isGryroActive
//            viewModel.showOverlay.value = viewModel.getCameraSetting().isOverlayActive
            viewModel.categoryDetails.value?.imageType = "Info"
            viewModel.hideLeveler.value = true
            viewModel.imageTypeInfo.value=true
            viewModel.showLeveler.value = false
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentInfoDialogBinding.inflate(inflater, container, false)
}