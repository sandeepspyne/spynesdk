package com.spyneai.threesixty.ui

import android.annotation.TargetApi
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
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
import com.spyneai.captureEvent
import com.spyneai.databinding.FragmentTrimVideoBinding
import com.spyneai.getVideoDuration
import com.spyneai.needs.AppConstants
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.videorecording.TrimmerUtils
import com.spyneai.videorecording.listener.SeekListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.HashMap

class TrimVideoFragment : BaseFragment<ThreeSixtyViewModel,FragmentTrimVideoBinding>(),SeekListener {

    private val TAG : String? = "TrimVideo"

    private var playerView: PlayerView? = null

    private var videoPlayer: SimpleExoPlayer? = null

    private var imagePlayPause: ImageView? = null

    private var totalDuration: Long = 0


    private var uri: Uri? = null

    private var txtStartDuration: TextView? = null
    private  var txtEndDuration: TextView? = null

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

    private var videoTrimmed = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerView = binding.playerViewLib
        imagePlayPause = binding.imagePlayPause

        txtStartDuration = binding.txtStartDuration
        txtEndDuration = binding.txtEndDuration

        if (requireActivity().intent.getBooleanExtra(AppConstants.FROM_VIDEO,false))
            setUpDrafts()
        else {
            seekHandler = Handler()

            initPlayer()

            setDataInView()

            binding.btnReshoot.setOnClickListener {
                requireActivity().finish()
            }

            binding.btnConfirm.setOnClickListener {
                trimVideo()
            }
        }
    }

    private fun setUpDrafts() {
        val intent = requireActivity().intent
        viewModel.fromDrafts = true

        viewModel.videoDetails?.apply {
            projectId = intent.getStringExtra(AppConstants.PROJECT_ID)
            skuName = intent.getStringExtra(AppConstants.SKU_NAME)
            skuId = intent.getStringExtra(AppConstants.SKU_ID)
            categoryId = intent.getStringExtra(AppConstants.CATEGORY_ID)
            categoryName = intent.getStringExtra(AppConstants.CATEGORY_NAME)!!
            frames =  intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0)
        }

        startNextActivity(intent.getStringExtra(AppConstants.VIDEO_PATH)!!)
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
            uri = Uri.parse(viewModel.videoDetails?.videoPath)

            totalDuration = requireContext().getVideoDuration(uri)
            imagePlayPause!!.setOnClickListener { v: View? -> onVideoClicked() }
            Objects.requireNonNull(playerView!!.videoSurfaceView)!!
                .setOnClickListener { v: View? -> onVideoClicked() }
            // initTrimData()
            binding.trimView.init(uri.toString(), totalDuration, this)

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
        try {
            seekHandler!!.removeCallbacks(updateSeekbar)
        }catch (e : Exception){
            requireContext().captureEvent(
                "Trim Exception",
                HashMap<String,Any?>()
                    .apply {
                        put("message",e.localizedMessage)
                    }
            )
        }
    }

    private fun trimVideo() {
        if (isValidVideo) {
            if (originalMinValue != lastMinValue || originalMaxValue != lastMaxValue){
                //not exceed given maxDuration if has given
                outputPath = getFileName().toString()
                //LogMessage.v("outputPath::" + outputPath + File(outputPath).exists())
                //LogMessage.v("sourcePath::$uri")
                videoPlayer!!.playWhenReady = false

                genVideoUsingMuxer(viewModel.videoDetails?.videoPath!!,outputPath,lastMinValue,lastMaxValue,false,true)
            }else{
                //do not trim send original video
                startNextActivity(viewModel.videoDetails?.videoPath!!)
            }

        } else Toast.makeText(
            requireContext(),
            "Video should not be smaller than" + " " + TrimmerUtils.getLimitedTimeFormatted(maxToGap),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getFileName(): String? {
        val path = requireActivity().getExternalFilesDir("Download")!!.path
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
                    fName + fileDateTime + "." + TrimmerUtils.getFileExtension(requireContext(), uri!!)
        )
        return newFile.toString()
    }

    private fun startNextActivity(path: String) {
        viewModel.videoDetails?.videoPath = path

        //update video path
        GlobalScope.launch(Dispatchers.IO) { viewModel.updateVideoPath() }

        Navigation.findNavController(binding.btnConfirm)
            .navigate(R.id.action_trimVideoFragment_to_threeSixtyBackgroundFragment)

        viewModel.title.value = "Select Background"
    }

    var updateSeekbar: Runnable = object : Runnable {
        override fun run() {
            try {
                currentDuration = videoPlayer!!.currentPosition
                binding.trimView.onVideoCurrentPositionUpdated(currentDuration)
                if (currentDuration >= lastMaxValue)
                    videoPlayer!!.playWhenReady = false

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
        seekTo(start);
    }

    override fun onSeek(type: SeekListener.Type, start: Long, end: Long) {
        lastMinValue = start
        lastMaxValue = end
        txtStartDuration!!.setText(TrimmerUtils.formatSeconds(start / 1000))
        txtEndDuration!!.setText(TrimmerUtils.formatSeconds(end / 1000))
    }



    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     * negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     * no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     * @param useVideo true if keep the video track from the source.
     * @throws IOException
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(IOException::class)
    private fun genVideoUsingMuxer(
        srcPath: String,
        dstPath: String,
        startMs: Long,
        endMs: Long,
        useAudio: Boolean,
        useVideo: Boolean
    ) {

        binding.progressBar.visibility = View.VISIBLE

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
            extractor.seekTo((startMs * 1000), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
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
                    Log.d(TAG, "genVideoUsingMuxer: "+"Saw input EOS.")
                    bufferInfo.size = 0
                    break
                } else {
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    if (endMs > 0 && bufferInfo.presentationTimeUs > endMs * 1000) {
                        //InstabugSDKLogger.d(TAG, "The current sample is over the trim end time.")
                        Log.d(TAG, "genVideoUsingMuxer: "+"The current sample is over the trim end time")
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

            Log.d(TAG, "genVideoUsingMuxer: "+dstPath)
            //deleting the old file
            //val file = File(srcPath)
            //file.delete()
            binding.progressBar.visibility = View.GONE
            startNextActivity(dstPath)
        } catch (e: IllegalStateException) {
            // Swallow the exception due to malformed source.
            //InstabugSDKLogger.w(TAG, "The source video file is malformed")
            Log.d(TAG, "genVideoUsingMuxer: "+"The source video file is malformed")
            binding.progressBar.visibility = View.GONE
            startNextActivity(srcPath)
        } finally {
            muxer.release()
        }
        return
    }

    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTrimVideoBinding.inflate(inflater, container, false)
}