package com.spyneai.videorecording

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.*
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.databinding.DataBindingUtil
import com.arthenica.mobileffmpeg.FFmpeg
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
import com.spyneai.databinding.ActivityTrimVideoBinding
import com.spyneai.videorecording.listener.SeekListener
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.activity_trim_video.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class TrimVideoActivity : AppCompatActivity() , SeekListener {
    private val TAG : String? = "TrimVideo"

    private var playerView: PlayerView? = null

    private var videoPlayer: SimpleExoPlayer? = null

    private var imagePlayPause: ImageView? = null

    private var totalDuration: Long = 0


    private var uri: Uri? = null

    private var txtStartDuration: TextView? = null
    private  var txtEndDuration:TextView? = null

    private var lastMinValue: Long = 0

    private var lastMaxValue: Long = 0
    private var originalMinValue : Long = 0
    private var originalMaxValue : Long = 0


    private var isValidVideo = true
    private  var isVideoEnded:kotlin.Boolean = false

    private var seekHandler: Handler? = null

    private var currentDuration: Long = 0

    private var outputPath: String = ""

    private  var maxToGap:kotlin.Long = 0

    private val fileName: String? = null

    private lateinit var binding : ActivityTrimVideoBinding
    private var videoTrimmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trim_video)

        if(intent.getIntExtra("shoot_mode", 0) == 1) binding.tvViewType.text = "Interior Back View"

        playerView = binding.playerViewLib
        imagePlayPause = binding.imagePlayPause

        txtStartDuration = binding.txtStartDuration
        txtEndDuration = binding.txtEndDuration

        seekHandler = Handler()

        initPlayer()

        setDataInView()

        btn_reshoot.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            if (videoTrimmed){

            }else{
                trimVideo()
            }

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
            uri = Uri.parse(intent.data.toString())
            
            totalDuration = getDuration(this, uri)
            imagePlayPause!!.setOnClickListener { v: View? -> onVideoClicked() }
            Objects.requireNonNull(playerView!!.videoSurfaceView)!!
                .setOnClickListener { v: View? -> onVideoClicked() }
            // initTrimData()
            trim_view.init(uri.toString(), totalDuration, this)

            lastMaxValue = totalDuration * 1000
            originalMaxValue = totalDuration * 1000
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
            videoPlayer!!.volume = 0F
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
                            binding.progressBar.visibility = View.GONE
                            binding.playerViewLib.visibility = View.VISIBLE
                            binding.imagePlayPause.visibility = View.VISIBLE
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
        if (isValidVideo) {
            if (originalMinValue != lastMinValue && originalMaxValue != lastMaxValue){
                //not exceed given maxDuration if has given
                outputPath = getFileName().toString()
                //LogMessage.v("outputPath::" + outputPath + File(outputPath).exists())
                //LogMessage.v("sourcePath::$uri")
                videoPlayer!!.playWhenReady = false
                val complexCommand: Array<String?>? = getAccurateCmd()

                execFFmpegBinary(complexCommand, true)
            }else{
                //do not trim send original video
                startNextActivity(intent.data?.toFile()?.path.toString())
            }

       } else Toast.makeText(
            this,
            "Video should not be smaller than" + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap),
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

        binding.progressBar.visibility = View.VISIBLE
        try {
            Thread {
                val result = FFmpeg.execute(command)
                if (result == 0) {

                    GlobalScope.launch(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                    }

                    startNextActivity(outputPath)

                    Log.d(TAG, "execFFmpegBinary: " + outputPath)
                } else {
                    Log.d(TAG, "execFFmpegBinary: " + "failed")
                    // Failed case:
                    // line 489 command fails on some devices in
                    // that case retrying with accurateCmt as alternative command
                    if (retry) {
                        val newFile = File(outputPath)
                        if (newFile.exists()) newFile.delete()
                        execFFmpegBinary(getAccurateCmd(), false)
                    } else {
                        GlobalScope.launch(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                        }
                        // start next activity in case of trimming failed
                        var file = intent.data?.toFile()
                       startNextActivity(file?.path.toString())
                    }
                }
            }.start()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun startNextActivity(path: String) {
        val intentPlay = Intent(
            this@TrimVideoActivity,
            SaveTrimmedVideoActivity::class.java
        )
        intentPlay.setData(intent.data)

        intentPlay.putExtra("uri", Uri.fromFile(File(path)).toString())
        intentPlay.putExtra("sku_id", intent.getStringExtra("sku_id"))
        intentPlay.putExtra("shoot_mode", intent.getIntExtra("shoot_mode", 0))
        startActivity(intentPlay)
    }

    private fun getAccurateCmd(): Array<String?>? {
//        return arrayOf(
//            uri.toString(), "-ss",
//            TrimmerUtils.formatCSeconds(lastMinValue),
//            "-vcodec copy " +
//                    "-acodec copy -t",
//            TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
//            "-strict -2", outputPath
//        )

//        return arrayOf(
//            "-ss", TrimmerUtils.formatCSeconds(lastMinValue), "-i", uri.toString(), "-t",
//            TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
//            "-async", "1", outputPath
//        )

        return arrayOf(
            "-ss", TrimmerUtils.formatCSeconds(lastMinValue / 1000), "-i", uri.toString(), "-t",
            TrimmerUtils.formatCSeconds(lastMaxValue / 1000 - lastMinValue / 1000),
            "-c", "copy", outputPath
        )
    }


    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer!!.currentPosition
                trim_view.onVideoCurrentPositionUpdated(currentDuration)
                if (currentDuration >= lastMaxValue)
                    videoPlayer!!.playWhenReady = false
//                if (currentDuration >= lastMaxValue)
//                    videoPlayer!!.playWhenReady = false;

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
        Log.d(TAG, "onSeek: "+start)
        Log.d(TAG, "onSeek: "+end)
    }

}