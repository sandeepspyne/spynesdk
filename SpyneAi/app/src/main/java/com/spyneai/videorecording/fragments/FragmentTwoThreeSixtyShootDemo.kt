package com.spyneai.videorecording.fragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.spyneai.R
import com.spyneai.databinding.FragmentTwoThreeSixtyShootDemoBinding
import kotlinx.android.synthetic.main.fragment_one_three_sixty_shoot_demo.*

import java.util.*


class FragmentTwoThreeSixtyShootDemo : Fragment() {

    private var isVideoPlaying: Boolean = false
    private var totalDuration: Long = 0
    private var uri: Uri? = null
    private lateinit var binding : FragmentTwoThreeSixtyShootDemoBinding
    private var playerView: PlayerView? = null
    private var videoPlayer: SimpleExoPlayer? = null
    private val TAG : String = "Exoplayer"

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    var isActive = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_two_three_sixty_shoot_demo,
            container,
            false
        )
        playerView = binding.playerViewLib;
        return binding.root
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
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            playerView?.setPlayer(videoPlayer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build()
                videoPlayer!!.setAudioAttributes(audioAttributes, true)
            }

            if(requireArguments()?.getInt("shoot_mode",0) == 1){
                 uri = RawResourceDataSource.buildRawResourceUri(R.raw.how_to_shoot_interior_back)
            }else{
                 uri = RawResourceDataSource.buildRawResourceUri(R.raw.how_to_shoot_interior_front)
            }

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
        if (!isActive && !videoPlayer?.playWhenReady!!){
            isActive = true
            videoPlayer!!.setPlayWhenReady(true)
        }else{
            //videoPlayer!!.setPlayWhenReady(true)
        }

    }


    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

     fun releasePlayer() {
//        playWhenReady = videoPlayer!!.getPlayWhenReady()
//        playbackPosition = videoPlayer!!.getCurrentPosition()
//        currentWindow = videoPlayer!!.getCurrentWindowIndex()
         try {
             videoPlayer!!.setPlayWhenReady(false);
             videoPlayer?.release()
             videoPlayer = null

             playerView?.visibility = View.GONE
         }catch (e:Exception){

         }
    }



}