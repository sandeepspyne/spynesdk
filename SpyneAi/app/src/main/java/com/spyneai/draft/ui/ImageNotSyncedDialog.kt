package com.spyneai.draft.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.Dialog360InteriorBinding
import com.spyneai.databinding.DialogOutdatedAppVersionBinding
import com.spyneai.databinding.DialogSyncDarftImagesBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.shoot.data.ShootViewModel

class ImageNotSyncedDialog : BaseDialogFragment<DraftViewModel, DialogSyncDarftImagesBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            if (it.getBoolean("sku_sync")){
                binding.tvReqCredit.text = "Project Sku's Not Synced"
                binding.tvDescription.text = "Please connect with internet to\n and retry to sync sku's"
            }
        }

        binding?.tvRetry?.setOnClickListener {
            viewModel.syncImages.value = if(viewModel.syncImages.value == null) true
            else !viewModel.syncImages.value!!

            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (dialog != null){
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
    }


    override fun getViewModel() = DraftViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogSyncDarftImagesBinding.inflate(inflater, container, false)
}