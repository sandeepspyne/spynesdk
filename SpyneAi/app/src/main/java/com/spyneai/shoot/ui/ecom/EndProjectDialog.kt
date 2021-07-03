package com.spyneai.shoot.ui.ecom

import android.R
import android.app.DialogFragment.STYLE_NO_FRAME
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment.STYLE_NO_FRAME
import com.spyneai.base.BaseDialogFragment
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
        dialog?.window?.setGravity(Gravity.BOTTOM)
        getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.WHITE));
    }
    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = EndProjectDialogBinding.inflate(inflater, container, false)

}