package com.spyneai.videorecording

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.MotionEventCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.spyneai.R
import com.bumptech.glide.request.target.Target
import com.spyneai.databinding.ActivityThreeSixtyInteriorViewBinding
import com.spyneai.videorecording.service.FramesHelper


class ThreeSixtyInteriorViewActivity : AppCompatActivity(),View.OnTouchListener {

    private lateinit var binding : ActivityThreeSixtyInteriorViewBinding
    private lateinit var framesList: ArrayList<String>
    private var mImageIndex: Int = 0
    private var mEndY: Int = 0
    private var mEndX: Int = 0
    private var mStartY: Int = 0
    private var mStartX: Int = 0
    var handler = Handler()
    var TAG = "UploadVideoTestService"
    lateinit var placeholder : Drawable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_three_sixty_interior_view)

        Log.d(TAG, "onCreate: "+intent.action)

//        framesList = intent.getStringArrayListExtra("frames")!!
        framesList = FramesHelper.hashMap.get(intent.action)!!

        binding.progressBarFront.visibility = View.VISIBLE

        mImageIndex = framesList.size / 2

        for ((index, url) in framesList.withIndex()) {

            Glide.with(this)
                .load(url)
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
                        Log.d(TAG, "onResourceReady: paseed " + index)

                        if (index == mImageIndex)
                            placeholder = resource!!

                        if (index == framesList.size - 1) {
                            binding.progressBarFront.visibility = View.GONE

                            loadImage(mImageIndex)
                        }

                        return false
                    }

                })
                //.override(300, 300)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()

     }

        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.ivFront.setOnTouchListener(this)


    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
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
                    if (mImageIndex >= framesList.size) mImageIndex = framesList.size - 1

                    //iv.setImageLevel(mImageIndex)
                    loadImage(mImageIndex)
                }
                if (mEndX - mStartX < -3) {
                    mImageIndex--
                    if (mImageIndex < 0) mImageIndex = 0
                    loadImage(mImageIndex)
                    //iv.setImageLevel(mImageIndex)
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

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({
            Log.d(TAG, "loadImage: " + index)

            Glide.with(this)
                .load(framesList.get(index))
                .placeholder(placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: failed")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        placeholder = resource!!

                        return false
                    }

                })
                .override(250, 250)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivFront)
        }, 10)

    }
}