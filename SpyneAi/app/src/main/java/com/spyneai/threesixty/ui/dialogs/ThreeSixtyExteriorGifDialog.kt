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
import com.spyneai.databinding.DialogAngleSelectionBinding
import com.spyneai.databinding.DialogThreeSixtyExteriorGifBinding
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.threesixty.data.ThreeSixtyViewModel


class ThreeSixtyExteriorGifDialog : BaseDialogFragment<ThreeSixtyViewModel,DialogThreeSixtyExteriorGifBinding>(){

    private var uri: Uri? = null
    private var playerView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null

    private var currentWindow = 0
    private var playbackPosition: Long = 0
    var isActive = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false
        playerView = binding.playerViewLib

        binding.tvGotit.setOnClickListener {
            viewModel.isDemoClicked.value = true

            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }

    /**
     * SettingUp exoplayer
     */
    private fun initPlayer() {
        try {
            videoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
            playerView?.setPlayer(videoPlayer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build()
                videoPlayer!!.setAudioAttributes(audioAttributes, true)
            }

            uri = RawResourceDataSource.buildRawResourceUri(R.raw.exterior_video)

            val mediaItem = MediaItem.fromUri(uri!!)
            videoPlayer!!.volume = 0F
            videoPlayer!!.setMediaItem(mediaItem)
            // videoPlayer!!.setPlayWhenReady(playWhenReady)

            videoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {

                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            videoPlayer!!.seekTo(0)
                        }

                    }
                }
            })

            videoPlayer!!.seekTo(currentWindow, playbackPosition)
            videoPlayer!!.prepare();



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isActive && (videoPlayer != null && !videoPlayer?.playWhenReady!!)){
            isActive = true
            videoPlayer!!.playWhenReady = true
        }

    }



    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            videoPlayer!!.setPlayWhenReady(false);
            videoPlayer?.release()
            videoPlayer = null

            playerView?.visibility = View.GONE
        }catch (e:Exception){

        }
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogThreeSixtyExteriorGifBinding.inflate(inflater, container, false)


}