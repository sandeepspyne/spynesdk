package com.spyneai.videorecording.ui

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginRight
import androidx.core.view.marginStart
import androidx.databinding.DataBindingUtil

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.spyneai.R
import com.spyneai.databinding.ViewTrimBinding
import com.spyneai.videorecording.listener.SeekListener
import kotlinx.android.synthetic.main.view_trim.view.*


class TrimView : ConstraintLayout, View.OnTouchListener {

    val TAG = javaClass.simpleName

    val VIDEO_EDIT_DISPLAY_FRAMES_COUNT  = 8;
    private var minDuration = 3000L
    private var maxDuration = Long.MAX_VALUE

    var layout: ViewTrimBinding

    var duration = 0L
    var start = 0L
    var end = 0L

    lateinit var videoUri: String
    lateinit var listener: SeekListener

    var maxDistanceBetweenBars = Long.MAX_VALUE

    public lateinit var startSeekHandle : View
    public lateinit var endSeekHandle : View


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

//        layout = ViewTrimBinding.inflate(LayoutInflater.from(context))

        layout = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.view_trim, this, true
        )

        Log.d(TAG, "view created")

        startSeekHandle = layout.ivStartSeekHandle
        endSeekHandle = layout.ivEndSeekHandle

        layout.ivStartSeekHandle.setOnTouchListener(this)
        layout.ivEndSeekHandle.setOnTouchListener(this)
    }

    fun init(videoUri: String, duration: Long,listener: SeekListener) {
        this.videoUri = videoUri
        this.duration = duration * 1000


        Log.d(TAG, "init: end: " + end)
        Log.d(TAG, "init: "+duration)

        this.listener = listener

        layout.llTrimFrameThumbnails.removeAllViews()
        initDisplayThumbnails()
    }

    fun disableTouch(context: Context) {
        layout.ivStartSeekHandle.setOnTouchListener(null)
        layout.ivEndSeekHandle.setOnTouchListener(null)

        layout.ivStartSeekHandle.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_left_saved_handle))
        layout.ivEndSeekHandle.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_right_saved_handle))
    }



    fun getVideoDuration(videoPath: String): Long {
        Log.d(TAG, "getVideoDuration: "+videoPath)
        var duration = 0L
        try {

            getMediaRetriever(videoPath).run {
                duration =
                    extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
                release()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return duration

    }

    fun getMediaRetriever(videoPath: String): MediaMetadataRetriever {
        return MediaMetadataRetriever().apply {
            try {
                if (videoPath != null && !videoPath.isEmpty())
                    setDataSource(videoPath)
            } catch (e: RuntimeException) {
                //e.printException()
            }

        }
    }

    private fun initDisplayThumbnails() {

        val diff = duration / VIDEO_EDIT_DISPLAY_FRAMES_COUNT

        for (i in 0 until VIDEO_EDIT_DISPLAY_FRAMES_COUNT) {


            var frameTimeStamp = diff * i

            var image = LayoutInflater.from(context).inflate(
                R.layout.view_image, layout.llTrimFrameThumbnails, false
            ) as ImageView

            layout.llTrimFrameThumbnails.addView(image)

            Glide.with(context).asBitmap()
                .load(videoUri)
                .apply(RequestOptions().frame(frameTimeStamp * 1000))
                .into(image)

        }

    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> listener.onSeekStarted()
            MotionEvent.ACTION_MOVE -> when (view.id) {
                R.id.ivStartSeekHandle -> onStartHandleMove(event.rawX)
                R.id.ivEndSeekHandle -> onEndHandleMove(event.rawX)
            }

            MotionEvent.ACTION_UP -> listener.onSeekEnd(getStartF(), getEndF())
        }
        return true
    }

    private fun getEndF(): Long {

        if (end == 0L)
            return duration

        return end / 100
    }

    private fun getStartF(): Long {

        return start / 100
    }

    private fun onStartHandleMove(rawX: Float) {
        var marginStart = rawX - layout.ivStartSeekHandle.width

        //so that it dont go out of container on left side
        if (marginStart < 0)
            marginStart = 0F

        //check to prevent from crossing endHandle
        else if (marginStart > (layout.llTrimFrameThumbnails.width - layout.ivEndSeekHandle.marginEnd)) {
            marginStart =
                (layout.llTrimFrameThumbnails.width - layout.ivEndSeekHandle.marginRight).toFloat()
        }


//        if (maxDuration != Long.MAX_VALUE) {
//
//            var distanceBetwenBars = (
//                    layout.llTrimFrameThumbnails.width
//                            - layout.ivEndSeekHandle.marginEnd
//                            - layout.ivStartSeekHandle.marginStart
//                    )
//
//            if (distanceBetwenBars >= maxDistanceBetweenBars && layout.ivStartSeekHandle.marginStart > marginStart) {
//
//                marginStart =
//                    (layout.llTrimFrameThumbnails.width.toFloat()
//                            - maxDistanceBetweenBars
//                            - ivEndSeekHandle.marginEnd)
//            }
//
//
//        }


        setMargin(ivStartSeekHandle, marginStart.toInt(), 0)

        var percentage = marginStart / layout.llTrimFrameThumbnails.width * 1.0f

        start = (percentage * 100 * duration).toLong()


        Log.d(TAG, "onStartHandleMove: " + percentage)

        listener.onSeek(SeekListener.Type.START, getStartF(), getEndF())
    }

    private fun onEndHandleMove(rawX: Float) {
        var marginEnd = rawX

        marginEnd =
            if (marginEnd <= (ivStartSeekHandle.marginStart + 2 * layout.ivStartSeekHandle.width))
                llTrimFrameThumbnails.width - ivStartSeekHandle.marginStart.toFloat()
            else
                llTrimFrameThumbnails.width - rawX + 2 * layout.ivEndSeekHandle.width

        if (marginEnd < 0)
            marginEnd = 0F

        setMargin(ivEndSeekHandle, 0, marginEnd.toInt())

        var percentage =
            (llTrimFrameThumbnails.width - marginEnd) * 1.0f / llTrimFrameThumbnails.width * 1.0f

        end = (percentage * 100 * duration).toLong()

        Log.d(TAG, "onEndHandleMove: $percentage")
        listener.onSeek(SeekListener.Type.END, getStartF(), getEndF())

    }

    private fun setMargin(view: ImageView, startMargin: Int, endMargin: Int) {
        var params = view.layoutParams as ConstraintLayout.LayoutParams
        params.marginStart = startMargin
        params.marginEnd = endMargin
        view.layoutParams = params
    }

    fun onVideoCurrentPositionUpdated(position: Long) {

        val x = (position * 1.0f / duration * 1.0f) * llTrimFrameThumbnails.width

        setMargin(layout.ivProgressStick, x.toInt(), 0)

    }


    fun setHandles(start: Long, end: Long) {

        this.start = start
        this.end = end

        resetHandles()
    }

    private fun resetHandles() {

        if (llTrimFrameThumbnails.width == 0) {
            waitForWidth()
            return
        }

        updateStartMarginsBasedOnDuration()
        updateEndMarginsBasedOnDuration()
    }

    private fun updateStartMarginsBasedOnDuration() {

        var startMargin =
            llTrimFrameThumbnails.width * (start * 1.0f / duration * 1.0f)

        Log.d(TAG, "updateStartMarginsBasedOnDuration: $startMargin")

        setMargin(ivStartSeekHandle, startMargin.toInt(), 0)

    }

    private fun updateEndMarginsBasedOnDuration() {

        var endMargin =
            llTrimFrameThumbnails.width * ((duration - end) * 1.0f / duration * 1.0f)

        Log.d(TAG, "updateEndMarginsBasedOnDuration: $endMargin")

        setMargin(ivEndSeekHandle, 0, endMargin.toInt())
    }

    private fun waitForWidth() {

        llTrimFrameThumbnails.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                llTrimFrameThumbnails.viewTreeObserver.removeOnGlobalLayoutListener(this)
                resetHandles()
            }
        })

    }


}


