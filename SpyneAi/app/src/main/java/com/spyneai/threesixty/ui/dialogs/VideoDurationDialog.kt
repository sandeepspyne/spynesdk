package com.spyneai.threesixty.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogVideoDurationBinding
import com.spyneai.threesixty.data.ThreeSixtyViewModel

class VideoDurationDialog : BaseDialogFragment<ThreeSixtyViewModel, DialogVideoDurationBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.tvReshoot.setOnClickListener {
            dismiss()
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogVideoDurationBinding.inflate(inflater, container, false)
}