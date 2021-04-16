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
import androidx.core.net.toFile
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar
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
import kotlinx.android.synthetic.main.activity_save_trimmed_video.*
import java.io.File

import java.util.*

class SaveTrimmedVideoActivity : AppCompatActivity(),SeekListener {

    private var videoPlayer: SimpleExoPlayer? = null
    private var playerView: PlayerView? = null
    private var imagePlayPause: ImageView? = null

    private lateinit var imageViews: Array<ImageView>

    private var totalDuration: Long = 0

    private val dialog: Dialog? = null

    private var uri: Uri? = null

    private var txtStartDuration: TextView? = null
    private  var txtEndDuration: TextView? = null

    private var seekbar: CrystalRangeSeekbar? = null

    private var lastMinValue: Long = 0

    private var lastMaxValue: Long = 0

    private val menuDone: MenuItem? = null

    private var seekbarController: CrystalSeekbar? = null

    private var isValidVideo = true
    private  var isVideoEnded:kotlin.Boolean = false

    private var seekHandler: Handler? = null

    private var currentDuration: Long = 0
    private  var lastClickedTime:kotlin.Long = 0



    private var outputPath: String? = null

    private val trimType = 0

    private val fixedGap: Long = 0
    private  var minGap:kotlin.Long = 0
    private  var minFromGap:kotlin.Long = 0
    private  var maxToGap:kotlin.Long = 0

    private val hidePlayerSeek = false
    private  var isAccurateCut:kotlin.Boolean = false
    private  var showFileLocationAlert:kotlin.Boolean = false


    private val fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_trimmed_video)

        playerView = player_view_lib
        imagePlayPause = image_play_pause

        txtStartDuration = txt_start_duration
        txtEndDuration = txt_end_duration

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
        myServiceIntent.putExtra("file_path", intent.data?.toFile()?.path)
        ContextCompat.startForegroundService(this, myServiceIntent)


//        var intent = Intent(this,RecordVideoActivity::class.java)
//        intent.putExtra("shoot_mode",1)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
    }

    /**
     * SettingUp exoplayer
     */
    private fun initPlayer() {
        try {
            videoPlayer = SimpleExoPlayer.Builder(this).build()
            playerView?.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            playerView?.setPlayer(videoPlayer)
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
            uri = Uri.parse(intent.data.toString())

            //  uri = Uri.parse(FileUtils.getPath(this, uri))

            //  LogMessage.v("VideoUri:: $uri")
            totalDuration = getDuration(this, uri)

            txtEndDuration!!.setText(TrimmerUtils.formatSeconds(5 * 1000 / 1000))

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
                            isVideoEnded = false
                            startProgress()
                            //LogMessage.v("onPlayerStateChanged: Ready to play.")
                        }
                        //Player.STATE_BUFFERING -> LogMessage.v("onPlayerStateChanged: STATE_BUFFERING.")
                        //Player.STATE_IDLE -> LogMessage.v("onPlayerStateChanged: STATE_IDLE.")
                        else -> {
                        }
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
//                if (!videoPlayer!!.playWhenReady) return
//                if (currentDuration <= lastMaxValue)
//                    seekbarController?.setMinStartValue(currentDuration.toFloat())?.apply()
//                else
//                    videoPlayer!!.playWhenReady = false;

            } finally {
                seekHandler!!.postDelayed(this, 1000)
            }
        }
    }

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