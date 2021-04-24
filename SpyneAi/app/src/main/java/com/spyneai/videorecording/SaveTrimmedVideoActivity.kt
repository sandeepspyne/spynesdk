package com.spyneai.videorecording

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
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
import com.spyneai.databinding.ActivitySaveTrimmedVideoBinding
import com.spyneai.videorecording.listener.SeekListener
import com.spyneai.videorecording.service.UploadVideoService
import kotlinx.android.synthetic.main.activity_save_trimmed_video.*

import java.util.*

class SaveTrimmedVideoActivity : AppCompatActivity(), SeekListener {

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

    private lateinit var binding : ActivitySaveTrimmedVideoBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_save_trimmed_video)

        if(intent.getIntExtra("shoot_mode",0) == 1) binding.tvViewType.text = "Interior Back View"

        playerView = binding.playerViewLib
        imagePlayPause = binding.imagePlayPause

        txtStartDuration = binding.txtStartDuration
        txtEndDuration = binding.txtEndDuration

        seekHandler = Handler()

        initPlayer()
        setDataInView()

        btn_cancel.setOnClickListener{ finish()}

        btn_save.setOnClickListener {
            startBackShoot()
        }

        iv_back.setOnClickListener{ finish() }
    }

    private fun startBackShoot() {
        //start uploading video
        val myServiceIntent = Intent(this, UploadVideoService::class.java)
        myServiceIntent.action = "START"
        myServiceIntent.putExtra("file_path", intent.getStringExtra("file_path"))
        myServiceIntent.putExtra("user_id",intent.getStringExtra("user_id"))
        myServiceIntent.putExtra("sku_id",intent.getStringExtra("sku_id"))
        myServiceIntent.putExtra("shoot_mode",intent.getIntExtra("shoot_mode",0))
        ContextCompat.startForegroundService(this, myServiceIntent)

        if (intent.getIntExtra("shoot_mode",0) == 0){
            var recordIntent = Intent(this,RecordVideoActivity::class.java)
            recordIntent.putExtra("sku_id",intent.getStringExtra("sku_id"))
            recordIntent.putExtra("user_id",intent.getStringExtra("user_id"))
            recordIntent.putExtra("shoot_mode",1)
            recordIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(recordIntent)
        }else{
            //start timer screen
            startActivity(Intent(this,ProcessVideoTimerActivity::class.java))
        }
    }

    /**
     * SettingUp exoplayer
     */
    private fun initPlayer() {
        try {
            videoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            playerView?.setPlayer(videoPlayer)
           // videoPlayer!!.volume = 0F
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

                uri = Uri.parse(intent.getStringExtra("file_path"))

            //  LogMessage.v("VideoUri:: $uri")
            totalDuration = getDuration(this, uri)
            totalDuration = totalDuration * 1000

            txtEndDuration!!.setText(TrimmerUtils.formatSeconds(totalDuration / 1000))

            imagePlayPause!!.setOnClickListener { v: View? -> onVideoClicked() }
            Objects.requireNonNull(playerView!!.videoSurfaceView)!!
                .setOnClickListener { v: View? -> onVideoClicked() }
            // initTrimData()
            trim_view.init(uri.toString(),totalDuration,this)
            trim_view.disableTouch(this)

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
                DefaultDataSourceFactory(this, getString(R.string.app_name))
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
                trim_view.onVideoCurrentPositionUpdated(currentDuration)
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
}