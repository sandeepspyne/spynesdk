package com.spyneai.threesixty.ui

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSaveVideoBinding
import com.spyneai.databinding.FragmentTrimVideoBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.videorecording.ProcessVideoTimerActivity
import com.spyneai.videorecording.RecordVideoActivity
import com.spyneai.videorecording.TrimmerUtils
import com.spyneai.videorecording.listener.SeekListener
import com.spyneai.videorecording.service.UploadVideoService
import java.util.*

class SaveVideoFragment : BaseFragment<ThreeSixtyViewModel,FragmentSaveVideoBinding>(),
    SeekListener {

    private var videoPlayer: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private var imagePlayPause: ImageView? = null

    private var totalDuration: Long = 0

    private var uri: Uri? = null

    private var txtStartDuration: TextView? = null
    private  var txtEndDuration: TextView? = null


    private var lastMinValue: Long = 0

    private var lastMaxValue: Long = 0


    private  var isVideoEnded:kotlin.Boolean = false

    private var seekHandler: Handler? = null

    private var currentDuration: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerView = binding.playerViewLib
        imagePlayPause = binding.imagePlayPause

        txtStartDuration = binding.txtStartDuration
        txtEndDuration = binding.txtEndDuration

        seekHandler = Handler()

        initPlayer()
        setDataInView()

        binding.btnCancel.setOnClickListener{ requireActivity().finish()}

        binding.btnSave.setOnClickListener {
            startBackShoot()
        }

        binding.ivBack.setOnClickListener{ requireActivity().finish() }
    }

    private fun startBackShoot() {

        viewModel.process360(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())

        viewModel.process360Res.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> Toast.makeText(requireContext(),"Processed",Toast.LENGTH_LONG).show()
                is Resource.Failure -> handleApiError(it)
                else -> {}
            }
        })

        //start uploading video
//        val myServiceIntent = Intent(requireContext(), UploadVideoService::class.java)
//        myServiceIntent.action = "START"
//        myServiceIntent.putExtra("file_path", viewModel.videoDetails.videoPath)
//        myServiceIntent.putExtra("user_id",intent.getStringExtra("user_id"))
//        myServiceIntent.putExtra("sku_id",intent.getStringExtra("sku_id"))
//        myServiceIntent.putExtra("shoot_mode",intent.getIntExtra("shoot_mode",0))
//        ContextCompat.startForegroundService(requireContext(), myServiceIntent)
//
//        if (intent.getIntExtra("shoot_mode",0) == 0){
//            var recordIntent = Intent(requireContext(), RecordVideoActivity::class.java)
//            recordIntent.putExtra("sku_id",intent.getStringExtra("sku_id"))
//            recordIntent.putExtra("user_id",intent.getStringExtra("user_id"))
//            recordIntent.putExtra("shoot_mode",1)
//            recordIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(recordIntent)
//        }else{
//            //start timer screen
//            startActivity(Intent(requireContext(), ProcessVideoTimerActivity::class.java))
//        }
    }

    /**
     * SettingUp exoplayer
     */
    private fun initPlayer() {
        try {
            videoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            playerView?.setPlayer(videoPlayer)
            videoPlayer!!.volume = 0F
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build()
                videoPlayer!!.setAudioAttributes(audioAttributes, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDataInView() {
        try {
            //uri = Uri.parse(intent.data.toString())

            uri = Uri.parse(viewModel.videoDetails.videoPath)

            //  LogMessage.v("VideoUri:: $uri")
            totalDuration = getDuration(requireActivity(), uri)
            totalDuration = totalDuration * 1000

            txtEndDuration!!.setText(TrimmerUtils.formatSeconds(totalDuration / 1000))

            imagePlayPause!!.setOnClickListener { v: View? -> onVideoClicked() }
            Objects.requireNonNull(playerView!!.videoSurfaceView)!!
                .setOnClickListener { v: View? -> onVideoClicked() }
            // initTrimData()
            binding.trimView.init(uri.toString(),totalDuration,this)
            binding.trimView.disableTouch(requireContext())

            lastMaxValue = totalDuration
            buildMediaSource()
            //loadThumbnails()
            //setUpSeekBar()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun onVideoClicked() {
        try {
            if (isVideoEnded) {
                seekTo(lastMinValue)
                videoPlayer!!.playWhenReady = true
                return
            }
            if (currentDuration - lastMaxValue > 0) seekTo(lastMinValue)
            videoPlayer!!.playWhenReady = !videoPlayer!!.playWhenReady
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun seekTo(sec: Long) {
        if (videoPlayer != null) videoPlayer!!.seekTo(sec)
    }


    fun getDuration(context: Activity?, videoPath: Uri?): Long {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoPath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeInMillisec = time!!.toLong()
            retriever.release()
            return timeInMillisec / 1000
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun buildMediaSource() {
        try {
            val dataSourceFactory: DataSource.Factory =
                DefaultDataSourceFactory(requireContext(), getString(R.string.app_name))
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(uri!!)
                )
            videoPlayer!!.addMediaSource(mediaSource)
            videoPlayer!!.prepare()
            videoPlayer!!.playWhenReady = true
            videoPlayer!!.addListener(object : Player.EventListener {
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    imagePlayPause!!.visibility = if (playWhenReady) View.GONE else View.VISIBLE
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            //LogMessage.v("onPlayerStateChanged: Video ended.")
                            imagePlayPause!!.visibility = View.VISIBLE
                            isVideoEnded = true
                        }
                        Player.STATE_READY -> {
                            //player visibility only when video is ready to play
                            binding.progressBar.visibility = View.GONE
                            binding.playerViewLib.visibility = View.VISIBLE
                            binding.imagePlayPause.visibility = View.VISIBLE
                            isVideoEnded = false
                            startProgress()
                            //LogMessage.v("onPlayerStateChanged: Ready to play.")
                        }
                        //Player.STATE_BUFFERING -> LogMessage.v("onPlayerStateChanged: STATE_BUFFERING.")
                        //Player.STATE_IDLE -> LogMessage.v("onPlayerStateChanged: STATE_IDLE.")

                    }
                }
            })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer!!.currentPosition
                binding.trimView.onVideoCurrentPositionUpdated(currentDuration)
            } finally {
                seekHandler!!.postDelayed(this, 1000)
            }
        }
    }

//    override fun onStop() {
//        super.onStop()
//        if (videoPlayer != null) videoPlayer!!.release()
//    }

    override fun onDestroy() {
        super.onDestroy()
        if (videoPlayer != null) videoPlayer!!.release()
        stopRepeatingTask()
    }

    fun startProgress() {
        updateSeekbar.run()
    }

    fun stopRepeatingTask() {
        seekHandler!!.removeCallbacks(updateSeekbar)
    }

    override fun onSeekStarted() {

    }

    override fun onSeekEnd(start: Long, end: Long) {

    }

    override fun onSeek(type: SeekListener.Type, start: Long, end: Long) {

    }


    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSaveVideoBinding.inflate(inflater, container, false)
}