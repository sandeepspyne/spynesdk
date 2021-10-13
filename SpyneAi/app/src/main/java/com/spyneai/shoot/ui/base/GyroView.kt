package com.spyneai.shoot.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.databinding.GyroViewBinding
import com.spyneai.needs.AppConstants
import kotlin.math.abs
import kotlin.math.roundToInt

class GyroView : FrameLayout {

    var binding: GyroViewBinding
    private var topConstraint = 0
    private var centerPosition = 0
    private var bottomConstraint = 0
    var isGyroOnCorrectAngle = false
    var cameraAngle = 45
    var cateoryName = ""

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

        val view = LayoutInflater.from(context).inflate(
            R.layout.gyro_view,null
        )

        this.addView(view)

        binding = GyroViewBinding.bind(this)
    }

    fun start(cateoryName : String){
        this.cateoryName = cateoryName

        if (cateoryName == "Footwear" ||
            cateoryName == "E-Commerce")
            binding.tvLevelIndicator.visibility = View.GONE

        binding.flLevelIndicator.visibility = View.VISIBLE

        getPreviewDimensions(binding.ivGryroRing!!, 1)
        getPreviewDimensions(binding.tvCenter!!, 2)
    }

    fun updateGryoView(appName : String,
                       roll  : Double,
                       pitch : Double,
                       movearrow : Boolean,
    rotatedarrow : Boolean){
        when (appName) {
            AppConstants.KARVI -> {
                if ((roll >= -95 && roll <= -85) && (pitch >= -5 && pitch <= 5)) {
                    gyroMeterOnLevel(true)

                } else {
                    gyroMeterOffLevel()

                    if (movearrow)
                        moveArrow(roll + 90)

                    if (rotatedarrow) {
                        if (pitch > 0) {
                            rotateArrow(pitch.minus(0).roundToInt())
                        } else {
                            rotateArrow(pitch.plus(0).roundToInt())
                        }
                    }
                }
            }

            AppConstants.SWIGGY -> {
                //hide moving line
                if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                    binding.tvLevelIndicator.visibility = View.GONE
                else
                    binding.tvLevelIndicator.visibility = View.VISIBLE

                if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                    pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 ||
                    (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100
                ) {
                    isGyroOnCorrectAngle = true
                    //angle 90
                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) {
                        cameraAngle = 90
                        gyroMeterOnLevel(false)
                    }
                    //angle 45
                    else if (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) {
                        cameraAngle = 45
                        gyroMeterOnLevel(false)
                        // angle 0
                    } else {
                        cameraAngle = 0
                        gyroMeterOnLevel(true)
                    }
                } else {
                    isGyroOnCorrectAngle = false
                    gyroMeterOffLevel()

                    if (movearrow) {
                        if (abs(roll.roundToInt()) < 100) {
                            moveArrow((pitch + 85).unaryMinus())
                        } else {
                            moveArrow(pitch + 85)
                        }
                    }

                    if (roll.roundToInt() == 1 || roll.roundToInt() == -1) {
                        if (roll.roundToInt() == 1) {
                            rotateArrow((pitch + 85).unaryMinus().roundToInt())
                        } else {
                            rotateArrow((pitch + 85).roundToInt())
                        }
                    }
                }
            }

            AppConstants.SPYNE_AI, AppConstants.SELL_ANY_CAR -> {
                when (cateoryName) {
                    "Automobiles", "Bikes" -> {
                        if ((roll >= -100 && roll <= -80) && (pitch >= -5 && pitch <= 5)) {
                            gyroMeterOnLevel(true)
                        } else {
                            gyroMeterOffLevel()

                            if (movearrow)
                                moveArrow(roll + 90)

                            if (rotatedarrow) {
                                if (pitch > 0) {
                                    rotateArrow(pitch.minus(0).roundToInt())
                                } else {
                                    rotateArrow(pitch.plus(0).roundToInt())
                                }
                            }
                        }
                    }

                    else -> {
                        //hide moving line
                        if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                            binding.tvLevelIndicator.visibility = View.GONE
                        else
                            binding.tvLevelIndicator.visibility = View.VISIBLE

                        if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                            pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88 ||
                            (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45) && abs(roll.roundToInt()) < 100
                        ) {
                            if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                                gyroMeterOnLevel(false)
                            else if (pitch.roundToInt() <= -40 && pitch.roundToInt() >= -45)
                                gyroMeterOnLevel(false)
                            else
                                gyroMeterOnLevel(true)
                        } else {
                            gyroMeterOffLevel()

                            if (movearrow) {
                                if (abs(roll.roundToInt()) < 100) {
                                    moveArrow((pitch + 85).unaryMinus())
                                } else {
                                    moveArrow(pitch + 85)
                                }
                            }

                            if (roll.roundToInt() == 1 || roll.roundToInt() == -1) {
                                if (roll.roundToInt() == 1) {
                                    rotateArrow((pitch + 85).unaryMinus().roundToInt())
                                } else {
                                    rotateArrow((pitch + 85).roundToInt())
                                }
                            }
                        }
                    }
                }
            }

            AppConstants.SWIGGYINSTAMART,
            AppConstants.UDAAN,
            AppConstants.FLIPKART,
            AppConstants.AMAZON,
            AppConstants.LAL_10,
            AppConstants.BATA,
            AppConstants.FLIPKART_GROCERY -> {
                //hide moving line
                if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                    binding.tvLevelIndicator.visibility = View.GONE
                else
                    binding.tvLevelIndicator.visibility = View.VISIBLE

                if ((pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3)) ||
                    pitch.roundToInt() <= -82 && pitch.roundToInt() >= -88
                ) {

                    if (pitch.roundToInt() == 0 || (pitch.roundToInt() <= -0 && pitch.roundToInt() >= -3))
                        gyroMeterOnLevel(false)
                    else
                        gyroMeterOnLevel(true)
                } else {
                    gyroMeterOffLevel()

                    if (movearrow) {
                        if (abs(roll.roundToInt()) < 100) {
                            moveArrow((pitch + 85).unaryMinus())
                        } else {
                            moveArrow(pitch + 85)
                        }
                    }

                    if (roll.roundToInt() == 1 || roll.roundToInt() == -1) {
                        if (roll.roundToInt() == 1) {
                            rotateArrow((pitch + 85).unaryMinus().roundToInt())
                        } else {
                            rotateArrow((pitch + 85).roundToInt())
                        }
                    }
                }
            }

            AppConstants.CARS24_INDIA,
            AppConstants.CARS24 -> {
                if ((roll >= -95 && roll <= -85) && (pitch >= -2 && pitch <= 2)) {
                    gyroMeterOnLevel(true)
                } else {
                    gyroMeterOffLevel()

                    if (movearrow)
                        moveArrow(roll + 90)

                    if (rotatedarrow) {
                        if (pitch > 0) {
                            rotateArrow(pitch.minus(0).roundToInt())
                        } else {
                            rotateArrow(pitch.plus(0).roundToInt())
                        }
                    }
                }
            }
            else -> {
                if ((roll >= -100 && roll <= -80) && (pitch >= -5 && pitch <= 5)) {
                    gyroMeterOnLevel(true)
                } else {
                    gyroMeterOffLevel()

                    if (movearrow)
                        moveArrow(roll + 90)

                    if (rotatedarrow) {
                        if (pitch > 0) {
                            rotateArrow(pitch.minus(0).roundToInt())
                        } else {
                            rotateArrow(pitch.plus(0).roundToInt())
                        }
                    }
                }
            }
        }
    }


    private fun gyroMeterOnLevel(removeAnimation: Boolean) {
        isGyroOnCorrectAngle = true
        if (removeAnimation) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(0f)
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        binding.tvLevelIndicator?.rotation = 0f

        binding.ivTopLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.ivBottomLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )

        binding.ivGryroRing?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.tvLevelIndicator?.background = ContextCompat.getDrawable(
            BaseApplication.getContext(),
            R.drawable.bg_gyro_level
        )

        binding.ivTopRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
        binding.ivBottomRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_in_level
            )
        )
    }

    private fun rotateArrow(roundToInt: Int) {
        binding.tvLevelIndicator?.rotation = roundToInt.toFloat()
    }


    private fun moveArrow(newRoll: Double) {
        if (newRoll > 0 && (centerPosition + newRoll) < bottomConstraint) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        if (newRoll < 0 && (centerPosition - newRoll) > topConstraint) {
            binding
                .tvLevelIndicator
                ?.animate()
                ?.translationY(newRoll.toFloat())
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }
    }

    private fun gyroMeterOffLevel() {
        isGyroOnCorrectAngle = false
        binding.ivTopLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.ivBottomLeft?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )

        binding.ivGryroRing?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.tvLevelIndicator?.background = ContextCompat.getDrawable(
            BaseApplication.getContext(),
            R.drawable.bg_gyro_error
        )

        binding.ivTopRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
        binding.ivBottomRight?.setColorFilter(
            ContextCompat.getColor(
                BaseApplication.getContext(),
                R.color.gyro_error_level
            )
        )
    }

    private fun getPreviewDimensions(view: View, type: Int) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                when (type) {
                    1 -> {
                        topConstraint = view.top
                        bottomConstraint = topConstraint + view.height
                    }

                    2 -> {
                        centerPosition = view.top
                    }
                }
            }
        })
    }
}