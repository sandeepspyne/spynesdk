package com.spyneai.shoot.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogFocusedHintBinding
import com.spyneai.databinding.DialogInteriorHintBinding
import com.spyneai.shoot.data.ShootViewModel

class MiscShotsDialog : BaseDialogFragment<ShootViewModel, DialogFocusedHintBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCancelable(false)

        binding.tvSkipFocused.setOnClickListener {
            viewModel.selectBackground.value = true
            dismiss()
        }

        binding.tvShootNowFocused.setOnClickListener {
            viewModel.startMiscShots.value = true
            dismiss()
        }

    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogFocusedHintBinding.inflate(inflater, container, false)
}