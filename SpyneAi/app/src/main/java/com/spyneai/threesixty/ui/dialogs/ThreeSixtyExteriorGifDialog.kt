package com.spyneai.threesixty.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.databinding.DialogThreeSixtyExteriorGifBinding
import com.spyneai.threesixty.data.ThreeSixtyViewModel


class ThreeSixtyExteriorGifDialog : BaseDialogFragment<ThreeSixtyViewModel,DialogThreeSixtyExteriorGifBinding>(){


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        Glide.with(this).asGif().load(R.raw.exterior).into(binding.ivExteriorGif)

        binding.tvGotit.setOnClickListener {
            viewModel.isDemoClicked.value = true

            dismiss()
        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogThreeSixtyExteriorGifBinding.inflate(inflater, container, false)


}