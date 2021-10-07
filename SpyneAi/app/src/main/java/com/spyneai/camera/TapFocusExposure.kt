package com.spyneai.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.FocusMeteringAction
import androidx.camera.view.PreviewView
import com.spyneai.R
import com.spyneai.camera2.ShootDimensions
import kotlin.math.roundToInt

class TapFocusExposure: FrameLayout,View.OnTouchListener{

    lateinit var viewFinder : PreviewView
    lateinit var  cameraControl: CameraControl
    lateinit var  cameraInfo: CameraInfo
    lateinit var  shootDimensions : ShootDimensions

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

    fun init(viewFinder : PreviewView,
             cameraControl: CameraControl,
              cameraInfo: CameraInfo,
              shootDimensions : ShootDimensions, ) {
        this.viewFinder = viewFinder
        this.cameraControl = cameraControl
        this.cameraInfo = cameraInfo
        this.shootDimensions = shootDimensions

        this.setOnTouchListener(this)
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> return true

            MotionEvent.ACTION_UP -> {
                // Get the MeteringPointFactory from PreviewView
                val factory = viewFinder.getMeteringPointFactory()

                // Create a MeteringPoint from the tap coordinates
                val point = factory.createPoint(event.x, event.y)


                // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                val action = FocusMeteringAction.Builder(point).build()

                // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                if (cameraControl != null) {
                    val listenable = cameraControl!!.startFocusAndMetering(action)

                    val layout =
                        LayoutInflater.from(context)
                            .inflate(R.layout.item_focus, null)
                    val ivFocus: ImageView = layout.findViewById(R.id.ivFocus)
                    //val tvExposure: TextView = layout.findViewById(R.id.tvExposure)

                    val rightSeekBar: SeekBar =
                        LayoutInflater.from(context)
                            .inflate(R.layout.item_exposure, null) as SeekBar

                    var seekClicked = false
                    val seekWidth = (30 * resources.displayMetrics.density).toInt()

                    val width = (70 * resources.displayMetrics.density).toInt()
                    val height = (80 * resources.displayMetrics.density).toInt()

                    val params = LayoutParams(width, height)
                    var seekParams =
                        LayoutParams(
                            seekWidth,
                           LayoutParams.WRAP_CONTENT
                        )

                    if (cameraInfo?.exposureState?.isExposureCompensationSupported) {
                        val exposureState = cameraInfo?.exposureState

                        rightSeekBar.max =
                            exposureState?.exposureCompensationRange?.upper?.times(10)!!

                        rightSeekBar.incrementProgressBy(1)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            rightSeekBar.setProgress(
                                exposureState?.exposureCompensationIndex?.times(
                                    10
                                )!!, false
                            )
                        }

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            rightSeekBar.min =
                                exposureState?.exposureCompensationRange?.lower?.times(
                                    10
                                )!!
                        }

                        //rightSeekBar.min = exposureState?.exposureCompensationRange?.lower!!

                        rightSeekBar?.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seek: SeekBar,
                                progress: Int, fromUser: Boolean
                            ) {
                                if (!seekClicked) {
                                    seekClicked = true
                                    seekParams.width =
                                        (150 * resources.displayMetrics.density).toInt()
                                    seekParams.leftMargin = params.leftMargin + width / 5
                                    rightSeekBar.layoutParams = seekParams
                                }

                                ivFocus.animate().cancel()
                                rightSeekBar.animate().cancel()

                                cameraControl!!.setExposureCompensationIndex(
                                    progress.times(0.10).roundToInt()
                                )
                                //tvExposure.text = progress.times(0.10).roundToInt().toString()
                                // write custom code for progress is changed
                            }

                            override fun onStartTrackingTouch(seek: SeekBar) {
                                // write custom code for progress is started
                            }

                            override fun onStopTrackingTouch(seek: SeekBar) {
                                startFadeAnimation(ivFocus, rightSeekBar)
                            }
                        })
                    } else {
                        rightSeekBar.visibility = View.GONE
                    }

                    removeAllViews()

                    params.leftMargin = when {
                        event.x.roundToInt() - width / 2 <= width -> 5
                        event.x.roundToInt() - width / 2 + width >= shootDimensions.previewWidth!! -> {
                            shootDimensions.previewWidth!! - width + 15
                        }
                        else -> event.x.roundToInt() - width / 2
                    }

                    params.topMargin = when {
                        event.y.roundToInt() - height / 2 <= width -> 5
                        event.y.roundToInt() - height / 2 >= shootDimensions.previewHeight!! -> {
                            shootDimensions.previewHeight!! - height
                        }
                        else -> event.y.roundToInt() - height / 2
                    }

                    ivFocus.layoutParams = params


                    seekParams.leftMargin = params.leftMargin + width + (8 * resources.displayMetrics.density).toInt()
                    seekParams.topMargin = params.topMargin + height / 3
                    rightSeekBar.layoutParams = seekParams

                    addView(layout)
                    addView(rightSeekBar)

                    startFadeAnimation(ivFocus, rightSeekBar)
                }

                return true
            }
            else ->                 // Unhandled event.
                return false
        }
        return true
    }

    private fun startFadeAnimation(ivFocus: ImageView, rightSeekBar: SeekBar) {
        handler?.removeCallbacksAndMessages(null)

        handler?.postDelayed({
            ivFocus.animate().alpha(0f).setDuration(1000)
                .setInterpolator(AccelerateInterpolator()).start()
            rightSeekBar.animate().alpha(0f).setDuration(1000)
                .setInterpolator(AccelerateInterpolator()).start()
        }, 2000)
    }
}