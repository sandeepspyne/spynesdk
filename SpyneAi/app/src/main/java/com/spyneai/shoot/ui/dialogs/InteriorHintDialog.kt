package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogInteriorHintBinding
import com.spyneai.databinding.DialogShootHintBinding
import com.spyneai.shoot.data.ShootViewModel

class InteriorHintDialog : BaseDialogFragment<ShootViewModel, DialogInteriorHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.tvSkip.setOnClickListener {
            viewModel.showMiscDialog.value = true
            dismiss()
        }

        binding.tvShootNowInterior.setOnClickListener {
            viewModel.startInteriorShots.value = true
            dismiss()
        }
    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogInteriorHintBinding.inflate(inflater, container, false)
}