package com.spyneai.videorecording

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.view.MotionEventCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.spyneai.R
import kotlinx.android.synthetic.main.activity_spin_view.*

class ThreeSixtyView : androidx.appcompat.widget.AppCompatImageView {

    private var array: Array<String> = emptyArray()
    private var mImageIndex: Int = 0
    private var mEndY: Int = 0
    private var mEndX: Int = 0
    private var mStartY: Int = 0
    private var mStartX: Int = 0
    var list: ArrayList<Uri> = ArrayList()
    var TAG = "spin"

    var myHandler = Handler()
    lateinit var placeholder : Drawable
    lateinit var listener : PreLoadListener
    var notifyPreLoad = true
    val preLoadMap : HashMap<Int,Boolean> = HashMap<Int,Boolean>()
    var preLoad = 5


    constructor(context: Context) : this(context, null){

    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){

    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }

     fun init(array : Array<String>,listener: PreLoadListener){
         this.array = array
         this.listener = listener

         mImageIndex = array.size / 2

         startPreload(mImageIndex)
     }

    private fun startPreload(index : Int) {
        val start = if (index - preLoad >= 0) index - preLoad else 0
        val end = if (index + preLoad <= array.size - 1) index + preLoad else array.size - 1

        for (i in start..end){

            if (preLoadMap.get(i) == null || !preLoadMap.get(i)!!){
                Glide.with(context)
                    .load(array.get(i))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            preLoadMap.put(i,false)
                            return true
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            preLoadMap.put(i,true)

                            if (notifyPreLoad && i == index)
                                placeholder = resource!!

                            if (notifyPreLoad && i == end) {
                                loadImage(index)
                                //notify
                                listener.onPreLoaded()
                                notifyPreLoad = false
                            }

                            return true
                        }

                    })
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload()
            }
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var action = MotionEventCompat.getActionMasked(event)

        when(action){
            MotionEvent.ACTION_DOWN -> {
                mStartX = event!!.x.toInt()
                mStartY = event.y.toInt()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                mEndX = event!!.x.toInt()
                mEndY = event.y.toInt()

                if (mEndX - mStartX > 3) {
                    mImageIndex++
                    if (mImageIndex >= array.size) mImageIndex = 0

                    //pre load
                    startPreload(mImageIndex)

                    loadImage(mImageIndex)
                }
                if (mEndX - mStartX < -3) {
                    mImageIndex--
                    if (mImageIndex < 0) mImageIndex = array.size - 1

                    //pre load
                    startPreload(mImageIndex)

                    loadImage(mImageIndex)

                }
                mStartX = event.x.toInt()
                mStartY = event.y.toInt()

                return true
            }

            MotionEvent.ACTION_UP -> {
                mEndX = event!!.x.toInt()
                mEndY = event.y.toInt()

                return true
            }

            MotionEvent.ACTION_CANCEL -> return true
            MotionEvent.ACTION_OUTSIDE -> return true
        }

        return super.onTouchEvent(event)
    }

    fun loadImage(index: Int){

        myHandler.removeCallbacksAndMessages(null)

        myHandler.postDelayed({

            Glide.with(context)
                .load(array.get(index))
                .placeholder(placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: failed")
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        placeholder = resource!!

                        return true
                    }

                })
                .override(250, 250)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this)
        }, 10)

    }
}