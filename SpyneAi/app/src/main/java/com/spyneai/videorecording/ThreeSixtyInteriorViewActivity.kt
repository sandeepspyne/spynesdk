package com.spyneai.videorecording

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.spyneai.databinding.DialogCopyEmbeddedCodeBinding
import com.spyneai.videorecording.fragments.DialogEmbedCode
import com.spyneai.videorecording.model.TSVParams
import com.spyneai.videorecording.service.FramesHelper


class ThreeSixtyInteriorViewActivity : AppCompatActivity(),View.OnTouchListener,View.OnClickListener {

    private lateinit var backFramesList: List<String>
    private lateinit var frontFramesList: List<String>
    private lateinit var binding : ActivityThreeSixtyInteriorViewBinding
    var handler = Handler()
    var TAG = "UploadVideoTestService"
    lateinit var tsvParamFront : TSVParams
    lateinit var tsvParamBack : TSVParams
    var shootId = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_three_sixty_interior_view)

//        framesList = intent.getStringArrayListExtra("frames")!!
       if (FramesHelper.framesMap != null && intent.action != null){
           shootId = FramesHelper.framesMap.get(intent.action)?.sku_id ?: ""
            frontFramesList =
               FramesHelper.framesMap.get(intent.action)?.video_data?.get(0)!!.processed_image_list
            backFramesList = FramesHelper.framesMap.get(intent.action)?.video_data?.get(1)!!.processed_image_list

       }


        if (frontFramesList != null && frontFramesList.size > 0){
            //load front image
            tsvParamFront = TSVParams()
            tsvParamFront.type = 0
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
//                                Glide.with(this@ThreeSixtyInteriorViewActivity)
//                                    .load(tsvParams.placeholder)
//                                    .placeholder(tsvParams.placeholder)
//                                    .into(binding.ivFront)

                                //show images and set listener
                                binding.ivShare.visibility = View.VISIBLE
                                binding.ivCopyLink.visibility = View.VISIBLE
                                binding.ivEmbbedCode.visibility = View.VISIBLE

                                binding.ivShare.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                                binding.ivCopyLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                                binding.ivEmbbedCode.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                                binding.ivFront.setOnTouchListener(this@ThreeSixtyInteriorViewActivity)

                            }else{
                                binding.progressBarBack.visibility = View.GONE
                                loadImage(tsvParams,binding.ivBackView)

//                                Glide.with(this@ThreeSixtyInteriorViewActivity)
//                                    .load(tsvParams.placeholder)
//                                    .placeholder(tsvParams.placeholder)
//                                    .into(binding.ivBackView)

                                //show images and set listener
                                binding.ivBackShare.visibility = View.VISIBLE
                                binding.ivBackCopyLink.visibility = View.VISIBLE
                                binding.ivBackEmbbedCode.visibility = View.VISIBLE

                                binding.ivBackShare.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                                binding.ivBackCopyLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                                binding.ivBackEmbbedCode.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                                binding.ivBackView.setOnTouchListener(this@ThreeSixtyInteriorViewActivity)
                            }
                        }

                        return false
                    }

                })
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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.iv_share -> {
                share(getCode(0))
            }

            R.id.iv_copy_link -> {
                copy("https://www.spyne.ai/shoots/shoot?skuId="+shootId+"&type=front")
            }

            R.id.iv_embbed_code -> {
                embed(getCode(0))
            }

            R.id.iv_back_share -> {
                share(getCode(1))
            }

            R.id.iv_back_copy_link -> {
                copy("https://www.spyne.ai/shoots/shoot?skuId="+shootId+"&type=back")
            }

            R.id.iv_back_embbed_code -> {
                embed(getCode(1))
            }
        }
    }

    private fun embed(code: String) {
            var args = Bundle()
        args.putString("code",code)

        var dialogCopyEmbeddedCode = DialogEmbedCode()
        dialogCopyEmbeddedCode.arguments = args
        dialogCopyEmbeddedCode.show(supportFragmentManager,"DialogEmbedCode")
    }

    private fun copy(link: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("link", link)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this,"Link copied successfully!",Toast.LENGTH_LONG).show()
    }

    private fun share(code: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, code)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun getCode(type : Int) : String {
        if (type == 0){
            return "<iframe \n" +
                    "  src=\"https://www.spyne.ai/shoots/shoot?skuId="+shootId+"&type=front\" \n" +
                    "  style=\"border:0; height: 100%; width: 100%;\" framerborder=\"0\"></iframe>"
        }else{
            return "<iframe \n" +
                    "  src=\"https://www.spyne.ai/shoots/shoot?skuId="+shootId+"&type=back\" \n" +
                    "  style=\"border:0; height: 100%; width: 100%;\" framerborder=\"0\"></iframe>"
        }

    }


}