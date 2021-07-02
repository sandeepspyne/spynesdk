package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.EndProjectDialogBinding
import com.spyneai.shoot.data.ShootViewModel


class EndProjectDialog : BaseDialogFragment<ShootViewModel, EndProjectDialogBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        binding.tvSkuName.text = viewModel.sku.value?.skuName.toString()
        binding.tvTotalSkuCaptured.text = viewModel.totalSkuCaptured.value
        binding.tvTotalImageCaptured.text = viewModel.totalImageCaptured.value

        binding.btNo.setOnClickListener {
            dismiss()
        }

        binding.btYes.setOnClickListener {
            dismiss()
            viewModel.showProjectDetail.value = true
        }

        binding.ivCloseDialog.setOnClickListener {
            dismiss()
        }

    }



    override fun onResume() {
        super.onResume()
        dialog?.getWindow()?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT);
    }
    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = EndProjectDialogBinding.inflate(inflater, container, false)

}