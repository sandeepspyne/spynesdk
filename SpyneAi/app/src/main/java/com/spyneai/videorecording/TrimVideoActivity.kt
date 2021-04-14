package com.spyneai.videorecording

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
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
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.activity_trim_video.*
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


class TrimVideoActivity : AppCompatActivity() ,SeekListener{
    private val TAG : String? = "TrimVideo"

    private var playerView: PlayerView? = null

    private val PER_REQ_CODE = 115

    private var videoPlayer: SimpleExoPlayer? = null

    private var imagePlayPause: ImageView? = null

    private lateinit var imageViews: Array<ImageView>

    private var totalDuration: Long = 0

    private val dialog: Dialog? = null

    private var uri: Uri? = null

    private var txtStartDuration: TextView? = null
    private  var txtEndDuration:TextView? = null

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
        setContentView(R.layout.activity_trim_video)

        playerView = findViewById<PlayerView>(R.id.player_view_lib)

        imagePlayPause = findViewById<ImageView>(R.id.image_play_pause)

        txtStartDuration = findViewById<TextView>(R.id.txt_start_duration)
        txtEndDuration = findViewById<TextView>(R.id.txt_end_duration)

        seekHandler = Handler()
        initPlayer()

        setDataInView()

        btn_reshoot.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            trimVideo()
        }

        iv_back.setOnClickListener{ finish()}
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
            imagePlayPause!!.setOnClickListener { v: View? -> onVideoClicked() }
            Objects.requireNonNull(playerView!!.videoSurfaceView)!!
                .setOnClickListener { v: View? -> onVideoClicked() }
            // initTrimData()
            trim_view.init(uri.toString(),totalDuration,this)

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




    private fun seekTo(sec: Long) {
        if (videoPlayer != null) videoPlayer!!.seekTo(sec)
    }


    fun startProgress() {
        updateSeekbar.run()
    }

    fun stopRepeatingTask() {
        seekHandler!!.removeCallbacks(updateSeekbar)
    }

    private fun trimVideo() {
        var test : String? = "saa";
        if (isValidVideo) {
            //not exceed given maxDuration if has given
            outputPath = getFileName()
            //LogMessage.v("outputPath::" + outputPath + File(outputPath).exists())
            //LogMessage.v("sourcePath::$uri")
            videoPlayer!!.playWhenReady = false
            //showProcessingDialog()
            val complexCommand: Array<String?>? = getAccurateCmd()

            execFFmpegBinary(complexCommand, true)
        } else Toast.makeText(
            this,
            "getString(R.string.txt_smaller)" + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getFileName(): String? {
        val path = getExternalFilesDir("Download")!!.path
        val calender = Calendar.getInstance()
        val fileDateTime = calender[Calendar.YEAR].toString() + "_" +
                calender[Calendar.MONTH] + "_" +
                calender[Calendar.DAY_OF_MONTH] + "_" +
                calender[Calendar.HOUR_OF_DAY] + "_" +
                calender[Calendar.MINUTE] + "_" +
                calender[Calendar.SECOND]
        var fName = "trimmed_video_"
        if (fileName != null && !fileName.isEmpty()) fName = fileName
        val newFile = File(
            path + File.separator +
                    fName + fileDateTime + "." + TrimmerUtils.getFileExtension(this, uri!!)
        )
        return newFile.toString()
    }

    private fun execFFmpegBinary(command: Array<String?>?, retry: Boolean) {
        try {
            Thread {
                val result = FFmpeg.execute(command)
                var s : String? = "sasas"
                if (result == 0) {
//                    dialog!!.dismiss()
                    if (showFileLocationAlert) Log.d(TAG, "execFFmpegBinary: show alert") else {

                        val intentPlay = Intent(this@TrimVideoActivity, SaveTrimmedVideoActivity::class.java);
                        intentPlay.setData(intent.data);

                        intentPlay.putExtra("uri",Uri.fromFile(File(outputPath)).toString())
                        startActivity(intentPlay);
                        Log.d(TAG, "execFFmpegBinary: "+outputPath)
                        //intent.putExtra(TrimVideo.TRIMMED_VIDEO_PATH, outputPath)
                        //setResult(RESULT_OK, intent)
                        //finish()
                    }
                } else if (result == 255) {
                    //LogMessage.v("Command cancelled")
                    if (dialog!!.isShowing) dialog.dismiss()
                } else {
                    Log.d(TAG, "execFFmpegBinary: "+"failed")
                    // Failed case:
                    // line 489 command fails on some devices in
                    // that case retrying with accurateCmt as alternative command
//                    if (retry && !isAccurateCut && compressOption == null) {
//                        val newFile = File(outputPath)
//                        if (newFile.exists()) newFile.delete()
//                        execFFmpegBinary(getAccurateCmd(), false)
//                    } else {
//                        if (dialog!!.isShowing) dialog.dismiss()
//                        runOnUiThread {
//                            Toast.makeText(
//                                this@TrimVideoActivity,
//                                "Failed to trim",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
                }
            }.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getAccurateCmd(): Array<String?>? {
        return arrayOf("-ss", TrimmerUtils.formatCSeconds(lastMinValue), "-i", uri.toString(), "-t",
            TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
            "-c", "copy", outputPath)
    }


    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer!!.currentPosition
                trim_view.onVideoCurrentPositionUpdated(currentDuration)
                if (currentDuration >= lastMaxValue)
                    videoPlayer!!.playWhenReady = false;

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

    override fun onSeekStarted() {

    }

    override fun onSeekEnd(start: Long, end: Long) {
//        lastMinValue = start
//        lastMaxValue = end

        seekTo(start);
    }

    override fun onSeek(type: SeekListener.Type, start: Long, end: Long) {
        lastMinValue = start
        lastMaxValue = end
        txtStartDuration!!.setText(TrimmerUtils.formatSeconds(start / 1000))
        txtEndDuration!!.setText(TrimmerUtils.formatSeconds(end / 1000))
        Log.d(TAG, "onSeek: "+start+":"+end)

    }

}