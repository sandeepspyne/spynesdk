package com.spyneai.threesixty.ui.dialogs

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.spyneai.R
import com.spyneai.base.BaseDialogFragment
import com.spyneai.shoot.data.ShootViewModel
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