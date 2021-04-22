package com.spyneai.videorecording

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
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
import com.spyneai.videorecording.model.TSVParams
import com.spyneai.videorecording.service.FramesHelper


class ThreeSixtyInteriorViewActivity : AppCompatActivity(),View.OnTouchListener {

    private lateinit var binding : ActivityThreeSixtyInteriorViewBinding
    var handler = Handler()
    var TAG = "UploadVideoTestService"
    lateinit var frontPlaceholder : Drawable
    lateinit var backPlaceholder : Drawable
    lateinit var tsvParamFront : TSVParams
    lateinit var tsvParamBack : TSVParams



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_three_sixty_interior_view)

        Log.d(TAG, "onCreate: "+intent.action)

//        framesList = intent.getStringArrayListExtra("frames")!!
        var frontFramesList =
            FramesHelper.framesMap.get(intent.action)?.video_data?.get(0)!!.processed_image_list
        var backFramesList = FramesHelper.framesMap.get(intent.action)?.video_data?.get(1)!!.processed_image_list



        if (frontFramesList != null && frontFramesList.size > 0){
            //load front image
            tsvParamFront = TSVParams()
            tsvParamFront.framesList = frontFramesList
            tsvParamFront.mImageIndex = frontFramesList.size / 2

            binding.progressBarFront.visibility = View.VISIBLE

            preLoad(tsvParamFront)
        }else{
            binding.progressBarFront.visibility = View.GONE
            Toast.makeText(this,"Frames list empty failed to load front view",Toast.LENGTH_LONG)
        }

        if (backFramesList != null && backFramesList.size > 0){
            //load back image
            tsvParamBack = TSVParams()
            tsvParamBack.type = 1
            tsvParamBack.framesList = backFramesList
            tsvParamBack.mImageIndex = backFramesList.size / 2

            binding.progressBarBack.visibility = View.VISIBLE

            preLoad(tsvParamBack)
        }else{
            binding.progressBarBack.visibility = View.GONE
            Toast.makeText(this,"Frames list empty failed to load back view",Toast.LENGTH_LONG)
        }



        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.ivFront.setOnTouchListener(this)
        binding.ivBackView.setOnTouchListener(this)


    }

    private fun preLoad(tsvParams: TSVParams) {
        for ((index, url) in tsvParams.framesList.withIndex()) {

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

                        if (index == tsvParams.mImageIndex){
                            tsvParams.placeholder = resource!!
                        }


                        if (index == tsvParams.framesList.size - 1) {
                            if (tsvParams.type == 0){
                                binding.progressBarFront.visibility = View.GONE
                                loadImage(tsvParams,binding.ivFront)
                            }else{
                                binding.progressBarBack.visibility = View.GONE
                                loadImage(tsvParams,binding.ivBackView)
                            }
                        }

                        return false
                    }

                })
                //.override(300, 300)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()

        }

    }

    private fun loadImage(tsvParams: TSVParams, imageView: ImageView) {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({

            Glide.with(this)
                .load(tsvParams.framesList.get(tsvParams.mImageIndex))
                .placeholder(tsvParams.placeholder)
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
                        tsvParams.placeholder = resource!!

                        return false
                    }

                })
                .override(250, 250)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }, 10)
    }



    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        var action = MotionEventCompat.getActionMasked(event)

        when(v?.id){
            R.id.iv_front -> {
                when(action){
                    MotionEvent.ACTION_DOWN -> {
                        tsvParamFront.mStartX = event!!.x.toInt()
                        tsvParamFront.mStartY = event.y.toInt()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        tsvParamFront.mEndX = event!!.x.toInt()
                        tsvParamFront.mEndY = event.y.toInt()

                        if (tsvParamFront.mEndX - tsvParamFront.mStartX > 3) {
                            tsvParamFront.mImageIndex++
                            if (tsvParamFront.mImageIndex >= tsvParamFront.framesList.size) tsvParamFront.mImageIndex = tsvParamFront.framesList.size - 1

                            //iv.setImageLevel(mImageIndex)
                            loadImage(tsvParamFront,binding.ivFront)

                        }
                        if (tsvParamFront.mEndX - tsvParamFront.mStartX < -3) {
                            tsvParamFront.mImageIndex--
                            if (tsvParamFront.mImageIndex < 0) tsvParamFront.mImageIndex = 0

                            loadImage(tsvParamFront,binding.ivFront)
                            //iv.setImageLevel(mImageIndex)
                        }
                        tsvParamFront.mStartX = event.x.toInt()
                        tsvParamFront.mStartY = event.y.toInt()

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        tsvParamFront.mEndX = event!!.x.toInt()
                        tsvParamFront.mEndY = event.y.toInt()

                        return true
                    }

                    MotionEvent.ACTION_CANCEL -> return true
                    MotionEvent.ACTION_OUTSIDE -> return true
                }
            }

            R.id.iv_back_view -> {
                when(action){
                    MotionEvent.ACTION_DOWN -> {
                        tsvParamFront.mStartX = event!!.x.toInt()
                        tsvParamFront.mStartY = event.y.toInt()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        tsvParamBack.mEndX = event!!.x.toInt()
                        tsvParamBack.mEndY = event.y.toInt()

                        if (tsvParamBack.mEndX - tsvParamBack.mStartX > 3) {
                            tsvParamBack.mImageIndex++
                            if (tsvParamBack.mImageIndex >= tsvParamBack.framesList.size) tsvParamBack.mImageIndex = tsvParamBack.framesList.size - 1

                            //iv.setImageLevel(mImageIndex)
                            loadImage(tsvParamBack,binding.ivBackView)

                        }
                        if (tsvParamBack.mEndX - tsvParamBack.mStartX < -3) {
                            tsvParamBack.mImageIndex--
                            if (tsvParamBack.mImageIndex < 0) tsvParamBack.mImageIndex = 0

                            loadImage(tsvParamBack,binding.ivBackView)
                            //iv.setImageLevel(mImageIndex)
                        }
                        tsvParamBack.mStartX = event.x.toInt()
                        tsvParamBack.mStartY = event.y.toInt()

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        tsvParamBack.mEndX = event!!.x.toInt()
                        tsvParamBack.mEndY = event.y.toInt()

                        return true
                    }

                    MotionEvent.ACTION_CANCEL -> return true
                    MotionEvent.ACTION_OUTSIDE -> return true
                }
            }
        }

        return super.onTouchEvent(event)
    }


}