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
import kotlinx.android.synthetic.main.activity_trim_video.*
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


class TrimVideoActivity : AppCompatActivity() {

    private val TAG : String? = "TrimVideo"

    private var playerView: PlayerView? = null


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



    private var outputPath: String? = null

    private val trimType = 0

    private val fixedGap: Long = 0
    private  var minGap:kotlin.Long = 0
    private  var minFromGap:kotlin.Long = 0
    private  var maxToGap:kotlin.Long = 0

    private val hidePlayerSeek = false
    private  var showFileLocationAlert:kotlin.Boolean = false


    private val fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim_video)

        playerView = findViewById<PlayerView>(R.id.player_view_lib)
        imagePlayPause = findViewById<ImageView>(R.id.image_play_pause)
        seekbar = findViewById<CrystalRangeSeekbar>(R.id.range_seek_bar)
        txtStartDuration = findViewById<TextView>(R.id.txt_start_duration)
        txtEndDuration = findViewById<TextView>(R.id.txt_end_duration)
        seekbarController = findViewById<CrystalSeekbar>(R.id.seekbar_controller)
        val imageOne = findViewById<ImageView>(R.id.image_one)
        val imageTwo = findViewById<ImageView>(R.id.image_two)
        val imageThree = findViewById<ImageView>(R.id.image_three)
        val imageFour = findViewById<ImageView>(R.id.image_four)
        val imageFive = findViewById<ImageView>(R.id.image_five)
        val imageSix = findViewById<ImageView>(R.id.image_six)
        val imageSeven = findViewById<ImageView>(R.id.image_seven)
        val imageEight = findViewById<ImageView>(R.id.image_eight)
        imageViews = arrayOf(
            imageOne, imageTwo, imageThree,
            imageFour, imageFive, imageSix, imageSeven, imageEight
        )
        seekHandler = Handler()
        initPlayer()

        setDataInView()

        btn_reshoot.setOnClickListener {
            finish()
        }

        btn_confirm.setOnClickListener {
            trimVideo()
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
            buildMediaSource()
            loadThumbnails()
            setUpSeekBar()
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

    /*
     *  loading thumbnails
     * */
    private fun loadThumbnails() {
        try {
            val diff = totalDuration / 8
            var sec = 1
            for (img in imageViews) {
                val interval = diff * sec * 1000000
                val options = RequestOptions().frame(interval)
                Glide.with(this)
                    .load(intent.data)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(img)
                if (sec < totalDuration) sec++
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSeekBar() {
        seekbar!!.visibility = View.VISIBLE
        txtStartDuration!!.visibility = View.VISIBLE
        txtEndDuration!!.visibility = View.VISIBLE
        seekbarController!!.setMaxValue(totalDuration.toFloat()).apply()
        seekbar!!.setMaxValue(totalDuration.toFloat()).apply()
        seekbar!!.setMaxStartValue(totalDuration.toFloat()).apply()
        lastMaxValue = if (trimType == 1) {
            seekbar!!.setFixGap(fixedGap.toFloat()).apply()
            totalDuration
        } else if (trimType == 2) {
            seekbar!!.setMaxStartValue(minGap.toFloat())
            seekbar!!.setGap(minGap.toFloat()).apply()
            totalDuration
        } else if (trimType == 3) {
            seekbar!!.setMaxStartValue(maxToGap.toFloat())
            seekbar!!.setGap(minFromGap.toFloat()).apply()
            maxToGap
        } else {
            seekbar!!.setGap(2f).apply()
            totalDuration
        }
        if (hidePlayerSeek) seekbarController!!.visibility = View.GONE
        seekbar!!.setOnRangeSeekbarFinalValueListener { minValue: Number?, maxValue: Number? ->
            if (!hidePlayerSeek) seekbarController!!.visibility = View.VISIBLE
        }
        seekbar!!.setOnRangeSeekbarChangeListener { minValue: Number, maxValue: Number ->
            val minVal = minValue as Long
            val maxVal = maxValue as Long
            if (lastMinValue != minVal) {
                seekTo(minValue)
                if (!hidePlayerSeek) seekbarController!!.visibility = View.INVISIBLE
            }
            lastMinValue = minVal
            lastMaxValue = maxVal
            txtStartDuration!!.setText(TrimmerUtils.formatSeconds(minVal))
            txtEndDuration!!.setText(TrimmerUtils.formatSeconds(maxVal))
            if (trimType == 3) setDoneColor(minVal, maxVal)
        }
        seekbarController!!.setOnSeekbarFinalValueListener { value: Number ->
            val value1 = value as Long
            if (value1 < lastMaxValue && value1 > lastMinValue) {
                seekTo(value1)
                return@setOnSeekbarFinalValueListener
            }


            if(value1 > lastMaxValue){
                seekbarController!!.setMinStartValue(lastMaxValue.toFloat()).apply()
            }else if(value1 < lastMinValue) {
                seekbarController!!.setMinStartValue(lastMinValue.toFloat()).apply()

                if (videoPlayer?.playWhenReady!!)
                    seekTo(lastMinValue)
            }

        }
    }

    private fun seekTo(sec: Long) {
        if (videoPlayer != null) videoPlayer!!.seekTo(sec * 1000)
    }

    private fun setDoneColor(minVal: Long, maxVal: Long) {
        try {
            if (menuDone == null) return
            //changed value is less than maxDuration
            if (maxVal - minVal <= maxToGap) {
                menuDone.icon.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.primary),
                        PorterDuff.Mode.SRC_IN
                    )
                isValidVideo = true
            } else {
                menuDone.icon.colorFilter =
                    PorterDuffColorFilter(
                        ContextCompat.getColor(this, R.color.primary),
                        PorterDuff.Mode.SRC_IN
                    )
                isValidVideo = false
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
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
        // The Folder location where all the files will be stored
         val path: String by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${Environment.DIRECTORY_DCIM}/CameraXTest/"
            } else {
                "${getExternalFilesDir(Environment.DIRECTORY_DCIM)?.path}/CameraXTest/"
            }
        }

        //val path = getExternalFilesDir("Download")!!.path
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
                if (result == 0) {
//                    dialog!!.dismiss()
                    if (showFileLocationAlert) Log.d(TAG, "execFFmpegBinary: show alert") else {

                        val intentPlay = Intent(
                            this@TrimVideoActivity,
                            PlayVideoActivity::class.java
                        );
                        intentPlay.setData(intent.data);

                        intentPlay.putExtra("uri", Uri.fromFile(File(outputPath)).toString())
                        startActivity(intentPlay);
                        Log.d(TAG, "execFFmpegBinary: " + outputPath)
                    }
                } else if (result == 255) {
                    //LogMessage.v("Command cancelled")
                    if (dialog!!.isShowing) dialog.dismiss()
                } else {
                    Log.d(TAG, "execFFmpegBinary: " + "failed")
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
        return arrayOf(
            "-ss", TrimmerUtils.formatCSeconds(lastMinValue), "-i", uri.toString(), "-t",
            TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
            "-c", "copy", outputPath
        )
//        return arrayOf(
//            "-ss", TrimmerUtils.formatCSeconds(lastMinValue), "-i", uri.toString(), "-t",
//            TrimmerUtils.formatCSeconds(lastMaxValue - lastMinValue),
//            "-async", "1", outputPath
//        )
    }


    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer!!.currentPosition / 1000
                if (!videoPlayer!!.playWhenReady) return
                if (currentDuration <= lastMaxValue)
                    seekbarController?.setMinStartValue(currentDuration.toFloat())?.apply()
                else
                    videoPlayer!!.playWhenReady = false;

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

    @Throws(IOException::class)
    private fun genVideoUsingMuxer(
        srcPath: String, dstPath: String,
        startMs: Int, endMs: Int, useAudio: Boolean, useVideo: Boolean
    ) {
        // Set up MediaExtractor to read from the source.
        val extractor = MediaExtractor()
        extractor.setDataSource(srcPath)
        val trackCount = extractor.trackCount
        // Set up MediaMuxer for the destination.
        val muxer: MediaMuxer
        muxer = MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        val indexMap: HashMap<Int, Int> = HashMap(trackCount)
        var bufferSize = -1
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            var selectCurrentTrack = false
            if (mime!!.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i)
                val dstIndex = muxer.addTrack(format)
                indexMap[i] = dstIndex
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    bufferSize = if (newSize > bufferSize) newSize else bufferSize
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE
        }
        // Set up the orientation and starting time for extractor.
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(srcPath)
        val degreesString = retrieverSrc.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
        )
        if (degreesString != null) {
            val degrees = degreesString.toInt()
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees)
            }
        }
        if (startMs > 0) {
            extractor.seekTo((startMs * 1000).toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        val offset = 0
        var trackIndex = -1
        val dstBuf: ByteBuffer = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        try {
            muxer.start()
            while (true) {
                bufferInfo.offset = offset
                bufferInfo.size = extractor.readSampleData(dstBuf, offset)
                if (bufferInfo.size < 0) {
                    //InstabugSDKLogger.d(TAG, "Saw input EOS.")
                    bufferInfo.size = 0
                    break
                } else {
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    if (endMs > 0 && bufferInfo.presentationTimeUs > endMs * 1000) {
                        //InstabugSDKLogger.d(TAG, "The current sample is over the trim end time.")
                        break
                    } else {
                        bufferInfo.flags = extractor.sampleFlags
                        trackIndex = extractor.sampleTrackIndex
                        muxer.writeSampleData(
                            indexMap[trackIndex]!!, dstBuf,
                            bufferInfo
                        )
                        extractor.advance()
                    }
                }
            }
            muxer.stop()

            //deleting the old file
            //val file = File(srcPath)
            //file.delete()
        } catch (e: IllegalStateException) {
            var test : String? = "sandeep";
            // Swallow the exception due to malformed source.
            //InstabugSDKLogger.w(TAG, "The source video file is malformed")
        } finally {
            muxer.release()
        }
        return
    }

}