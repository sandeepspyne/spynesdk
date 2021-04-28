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
import com.spyneai.activity.DashboardActivity
import com.spyneai.databinding.ActivityThreeSixtyInteriorViewBinding



import com.spyneai.videorecording.fragments.DialogEmbedCode
import com.spyneai.videorecording.model.TSVParams
import com.spyneai.videorecording.service.FramesHelper


class ThreeSixtyInteriorViewActivity : AppCompatActivity(),View.OnTouchListener,View.OnClickListener {

    private lateinit var backFramesList: List<String>
    private lateinit var frontFramesList: List<String>
    private lateinit var binding : ActivityThreeSixtyInteriorViewBinding
    var frontHandler = Handler()
    var backHandler = Handler()
    var TAG = "UploadVideoTestService"

    var mFrontImageIndex: Int = 0
    var mFrontEndY: Int = 0
    var mFrontEndX: Int = 0
    var mFrontStartY: Int = 0
    var mFrontStartX: Int = 0
    var frontplaceholder : Drawable? = null


    var mBackImageIndex: Int = 0
    var mBackEndY: Int = 0
    var mBackEndX: Int = 0
    var mBackStartY: Int = 0
    var mBackStartX: Int = 0
    var backPlaceholder : Drawable? = null

    //lateinit var tsvParamFront : TSVParams
    //lateinit var tsvParamBack : TSVParams
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
            mFrontImageIndex = frontFramesList.size / 2
            //load front image
//            tsvParamFront = TSVParams()
//            tsvParamFront.type = 0
//            tsvParamFront.framesList = frontFramesList
//            tsvParamFront.mImageIndex = frontFramesList.size / 2

            //binding.sv.visibility = View.VISIBLE

            binding.svFront.startShimmer()
            preLoadFront()
        }else{
            //binding.progressBarFront.visibility = View.GONE
                binding.svFront.stopShimmer()
            binding.svFront.visibility = View.GONE
            Toast.makeText(this,"Frames list empty failed to load front view",Toast.LENGTH_LONG)
        }

        if (backFramesList != null && backFramesList.size > 0){
            //load back image
//            tsvParamBack = TSVParams()
//            tsvParamBack.type = 1
//            tsvParamBack.framesList = backFramesList
//            tsvParamBack.mImageIndex = backFramesList.size / 2

            mBackImageIndex = backFramesList.size / 2

            //binding.progressBarBack.visibility = View.VISIBLE
            binding.svBack.startShimmer()

            preLoadBack()
        }else{
           // binding.progressBarBack.visibility = View.GONE
            binding.svBack.stopShimmer()
            binding.svBack.visibility = View.GONE
            Toast.makeText(this,"Frames list empty failed to load back view",Toast.LENGTH_LONG)
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (intent.getIntExtra("back_press_type",0) == 1){
            var dashboardIntent = Intent(this, DashboardActivity::class.java)
            dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(dashboardIntent)
        }else{
            super.onBackPressed()
        }

    }

    private fun preLoadFront() {
        for ((index, url) in frontFramesList.withIndex()) {

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
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //Log.d(TAG, "onResourceReady: paseed " + index)

                        if (index == mFrontImageIndex) {
                            frontplaceholder= resource!!
                        }

                        if (index == frontFramesList.size - 1) {
                            //binding.progressBarFront.visibility = View.GONE

                            binding.svFront.stopShimmer()
                            binding.svFront.visibility = View.GONE


                            loadImageFront()

                            //show images and set listener
                            binding.clFront.visibility = View.VISIBLE
                            showGoToHomeButton()

                            binding.tvShare.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                            binding.ivShare.setOnClickListener(this@ThreeSixtyInteriorViewActivity)



                            binding.ivCopyLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                            binding.tvCopyLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                            binding.ivShareLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                            binding.tvShareLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                            binding.ivFront.setOnTouchListener(this@ThreeSixtyInteriorViewActivity)

                        }

                        return false
                    }

                })
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()

        }

    }


    private fun preLoadBack() {
        for ((index, url) in backFramesList.withIndex()) {

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
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                       // Log.d(TAG, "onResourceReady: paseed " + index)

                        if (index == mBackImageIndex){
                            backPlaceholder = resource!!
                        }

                        if (index == backFramesList.size - 1) {
                            //binding.progressBarBack.visibility = View.GONE
                            binding.svBack.stopShimmer()
                            binding.svBack.visibility = View.GONE

                            loadImageBack()

                            //show images and set listener
                            binding.clBack.visibility = View.VISIBLE
                            showGoToHomeButton()

                            binding.tvBackShare.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                            binding.ivBackShare.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                            binding.tvBackCopyLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                            binding.ivBackCopyLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                            binding.ivBackShareLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)
                            binding.tvBackShareLink.setOnClickListener(this@ThreeSixtyInteriorViewActivity)

                            binding.ivBackView.setOnTouchListener(this@ThreeSixtyInteriorViewActivity)

                        }

                        return false
                    }

                })
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()

        }

    }


//    private fun loadImage(tsvParams: TSVParams, imageView: ImageView) {
//
//        handler.removeCallbacksAndMessages(null)
//
//        handler.postDelayed({
//
//            try {
//                var glide = Glide.with(this)
//                    .load(tsvParams.framesList.get(tsvParams.mImageIndex))
//
//                if (tsvParams.placeholder != null)
//                    glide.placeholder(tsvParams.placeholder)
//
//                glide.listener(object : RequestListener<Drawable> {
//                    override fun onLoadFailed(
//                        e: GlideException?,
//                        model: Any?,
//                        target: com.bumptech.glide.request.target.Target<Drawable>?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        Log.d(TAG, "onResourceReady: failed")
//                        return false
//                    }
//
//                    override fun onResourceReady(
//                        resource: Drawable?,
//                        model: Any?,
//                        target: com.bumptech.glide.request.target.Target<Drawable>?,
//                        dataSource: DataSource?,
//                        isFirstResource: Boolean
//                    ): Boolean {
//                        Log.d(TAG, "loading: a"+tsvParams.type)
//                        tsvParams.placeholder = resource!!
//
//                        return false
//                    }
//
//                })
//                    //.override(250, 250)
//                    .dontAnimate()
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(imageView)
//
//
//                if (binding.ivFront.visibility == View.INVISIBLE) binding.ivFront.visibility = View.VISIBLE
//            } catch (ex: UninitializedPropertyAccessException) {
//                Log.d(TAG, "loadImage: ex " + tsvParams.type)
//                Log.d(TAG, "loadImage: ex " + ex.localizedMessage)
//
//            }
//        }, 10)
//    }

    private fun loadImageFront() {

        frontHandler.removeCallbacksAndMessages(null)

        frontHandler.postDelayed({

            try {
                var glide = Glide.with(this)
                    .load(frontFramesList.get(mFrontImageIndex))

                if (frontplaceholder != null)
                    glide.placeholder(frontplaceholder)

                glide.listener(object : RequestListener<Drawable> {
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
                        Log.d(TAG, "loading: a 0")
                        frontplaceholder = resource!!

                        return false
                    }

                })
                    .override(250, 250)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivFront)


                if (binding.ivFront.visibility == View.INVISIBLE) binding.ivFront.visibility = View.VISIBLE
            } catch (ex: UninitializedPropertyAccessException) {

                Log.d(TAG, "loadImage: ex " + ex.localizedMessage)

            }
        }, 10)
    }


    private fun loadImageBack() {

        backHandler.removeCallbacksAndMessages(null)

        backHandler.postDelayed({

            try {
                var glide = Glide.with(this)
                    .load(backFramesList.get(mBackImageIndex))

                if (backPlaceholder != null)
                    glide.placeholder(backPlaceholder)

                glide.listener(object : RequestListener<Drawable> {
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
                        Log.d(TAG, "loading: a 1")
                        backPlaceholder = resource!!

                        return false
                    }

                })
                    .override(250, 250)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.ivBackView)


                if (binding.ivBackView.visibility == View.INVISIBLE) binding.ivBackView.visibility = View.VISIBLE
            } catch (ex: UninitializedPropertyAccessException) {
                Log.d(TAG, "loadImage: ex " + ex.localizedMessage)

            }
        }, 10)
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {

        var action = MotionEventCompat.getActionMasked(event)

        when(v?.id){
            R.id.iv_front -> {
                when(action){
                    MotionEvent.ACTION_DOWN -> {
                        mFrontStartX= event!!.x.toInt()
                        mFrontStartY = event.y.toInt()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        mFrontEndX = event!!.x.toInt()
                        mFrontEndY = event.y.toInt()

                        if (mFrontEndX - mFrontStartX > 3) {
                            mFrontImageIndex++

                            if (mFrontImageIndex >= frontFramesList.size) mFrontImageIndex = frontFramesList.size - 1

                            //iv.setImageLevel(mImageIndex)
                            loadImageFront()

                        }
                        if (mFrontEndY - mFrontStartX < -3) {
                            mFrontImageIndex--
                            if (mFrontImageIndex < 0) mFrontImageIndex = 0

                            loadImageFront()
                            //iv.setImageLevel(mImageIndex)
                        }
                        mFrontStartX = event.x.toInt()
                        mFrontStartY = event.y.toInt()

                        if (binding.clShareFront.visibility == View.VISIBLE) binding.clShareFront.visibility = View.GONE

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        mFrontEndX = event!!.x.toInt()
                        mFrontEndY = event.y.toInt()


                        return true
                    }

                    MotionEvent.ACTION_CANCEL -> return true
                    MotionEvent.ACTION_OUTSIDE -> return true
                }
            }

            R.id.iv_back_view -> {
                when(action){
                    MotionEvent.ACTION_DOWN -> {
                        mBackStartX= event!!.x.toInt()
                        mBackStartY = event.y.toInt()
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        mBackEndX = event!!.x.toInt()
                        mBackEndY = event.y.toInt()

                        if (mBackEndX - mBackStartX > 3) {
                            mBackImageIndex++

                            if (mBackImageIndex >= backFramesList.size) mBackImageIndex= backFramesList.size - 1

                            //iv.setImageLevel(mImageIndex)
                            loadImageBack()

                        }
                        if (mBackEndX - mBackStartX < -3) {
                            mBackImageIndex--
                            if (mBackImageIndex < 0) mBackImageIndex = 0

                            loadImageBack()
                            //iv.setImageLevel(mImageIndex)
                        }
                        mBackStartX = event.x.toInt()
                        mBackStartY = event.y.toInt()

                        if (binding.clShareBack.visibility == View.VISIBLE) binding.clShareBack.visibility = View.GONE

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        mBackEndX = event!!.x.toInt()
                        mBackEndY = event.y.toInt()

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
            R.id.tv_share,R.id.iv_share-> binding.clShareFront.visibility = View.VISIBLE

            R.id.tv_back_share,R.id.iv_back_share-> binding.clShareBack.visibility = View.VISIBLE

            R.id.iv_copy_link, R.id.tv_copy_link -> {
                binding.clShareFront.visibility = View.GONE
                embed(getCode(0))
            }

            R.id.iv_share_link,R.id.tv_share_link -> {
                binding.clShareFront.visibility = View.GONE
                share(getCode(0))
            }

            R.id.iv_back_copy_link, R.id.tv_back_copy_link -> {
                binding.clShareBack.visibility = View.GONE
                embed(getCode(1))
            }

            R.id.iv_back_share_link,R.id.tv_back_share_link -> {
                binding.clShareBack.visibility = View.GONE
                share(getCode(1))
            }

            R.id.tv_go_to_home -> {
                var dashboardIntent = Intent(this, DashboardActivity::class.java)
                dashboardIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(dashboardIntent)
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

    private fun showGoToHomeButton(){
        binding.tvShowIframe.visibility = View.VISIBLE
        binding.tvGoToHome.visibility = View.VISIBLE

        binding.tvGoToHome.setOnClickListener(this)
    }


}