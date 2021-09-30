package com.spyneai.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.spyneai.BaseApplication
import com.spyneai.R
import com.spyneai.databinding.LayoutGyroViewBinding
import com.spyneai.databinding.ViewTrimBinding

class GyroView : FrameLayout {

    var layout: LayoutGyroViewBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

       val view = LayoutInflater.from(context).inflate(R.layout.layout_gyro_view,null)

        layout = LayoutGyroViewBinding.bind(view)
    }

    fun gyroOffLevel(){
        val color = ContextCompat.getColor(
            BaseApplication.getContext(),
            R.color.gyro_error_level
        )
        layout.apply {
            ivTopLeft?.setColorFilter(color)
            ivBottomLeft?.setColorFilter(color)
            ivGryroRing?.setColorFilter(color)
            tvLevelIndicator?.setColorFilter(color)
            ivTopRight?.setColorFilter(color)
            ivBottomRight?.setColorFilter(color)
        }
    }

    fun gyroOnLevel(removeAnimation: Boolean) {
        if (removeAnimation) {
            layout
                .tvLevelIndicator
                ?.animate()
                ?.translationY(0f)
                ?.setInterpolator(AccelerateInterpolator())?.duration = 0
        }

        val color = ContextCompat.getColor(
            BaseApplication.getContext(),
            R.color.gyro_in_level
        )
        layout.apply {
            tvLevelIndicator?.rotation = 0f

            ivTopLeft?.setColorFilter(color)
            ivBottomLeft?.setColorFilter(color)
            ivGryroRing?.setColorFilter(color)
            tvLevelIndicator?.setColorFilter(color)
            ivTopRight?.setColorFilter(color)
            ivBottomRight?.setColorFilter(color)
        }
    }
}