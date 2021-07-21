package com.spyneai.threesixty.ui

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
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.spyneai.R
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.databinding.ActivityThreeSixtyExteriorBinding
import com.spyneai.databinding.ActivityThreeSixtyInteriorViewBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.threesixty.data.model.ProcessedFrames
import com.spyneai.videorecording.fragments.DialogEmbedCode
import com.spyneai.videorecording.model.TSVParams
import com.spyneai.videorecording.service.FramesHelper

class ThreeSixtyExteriorActivity : AppCompatActivity(),View.OnTouchListener,View.OnClickListener {

    lateinit var binding : ActivityThreeSixtyExteriorBinding
    private lateinit var frontFramesList: List<String>
    lateinit var tsvParamFront : TSVParams
    var handler = Handler()
    var shootId = ""
    var TAG = "ThreeSixtyExteriorActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_sixty_exterior)

        binding = ActivityThreeSixtyExteriorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val shootViewModel = ViewModelProvider(this, ViewModelFactory()).get(ProcessedViewModel::class.java)

        shootId = intent.getStringExtra("sku_id")!!

        shootViewModel.getImagesOfSku(Utilities.getPreference(this,AppConstants.AUTH_KEY).toString(),
        intent.getStringExtra("sku_id")!!)

        shootViewModel.imagesOfSkuRes.observe(this,{
            when(it) {
                is com.spyneai.base.network.Resource.Success -> {
                    Utilities.hideProgressDialog()

                    frontFramesList = it.value.data.map { it.output_image_lres_url }

                    tsvParamFront = TSVParams()
                    tsvParamFront.type = 0
                    tsvParamFront.framesList = frontFramesList
                    tsvParamFront.mImageIndex = frontFramesList.size / 2

                    binding.svFront.startShimmer()

                    preLoadFront(tsvParamFront)

                    //load front image
                    Glide.with(this)
                        .load(frontFramesList.get(tsvParamFront.mImageIndex))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.ivFront.visibility = View.VISIBLE
                                binding.svFront.stopShimmer()
                                binding.svFront.visibility = View.GONE

                                //show images and set listener
                                binding.clFront.visibility = View.VISIBLE

                                showGoToHomeButton()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                binding.ivFront.visibility = View.VISIBLE
                                binding.svFront.stopShimmer()
                                binding.svFront.visibility = View.GONE

                                //show images and set listener
                                binding.clFront.visibility = View.VISIBLE
                                showGoToHomeButton()
                                return false
                            }

                        })
                        .into(binding.ivFront)
                }

                is com.spyneai.base.network.Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    Toast.makeText(this,"Sever not responding please try again..",Toast.LENGTH_LONG).show()
                }

                is com.spyneai.base.network.Resource.Loading -> {
                    Utilities.showProgressDialog(this)
                }
            }
        })

    }


    private fun preLoadFront(tsvParams: TSVParams) {
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
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: paseed " + index)


                        if (index == tsvParams.framesList.size - 1) {

                            binding.ivFront.setOnTouchListener(this@ThreeSixtyExteriorActivity)
                        }

                        return false
                    }

                })
                .dontAnimate()
                .override(250, 250)
                .preload()

        }

        setListeners()

    }


    private fun setListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        //front view listener
        binding.tvShare.setOnClickListener(this@ThreeSixtyExteriorActivity)
        binding.ivShare.setOnClickListener(this@ThreeSixtyExteriorActivity)


        binding.ivCopyLink.setOnClickListener(this@ThreeSixtyExteriorActivity)
        binding.tvCopyLink.setOnClickListener(this@ThreeSixtyExteriorActivity)

        binding.ivShareLink.setOnClickListener(this@ThreeSixtyExteriorActivity)
        binding.tvShareLink.setOnClickListener(this@ThreeSixtyExteriorActivity)

    }

    private fun loadImage(tsvParams: TSVParams, imageView: ImageView) {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({

            Log.d(TAG, "loading: a"+tsvParams.type)
            Log.d(TAG, "loading: a"+tsvParams.framesList.get(tsvParams.mImageIndex))


            try {
                var glide = Glide.with(this)
                    .load(tsvParams.framesList.get(tsvParams.mImageIndex))

                if (tsvParams.placeholder != null)
                    glide.placeholder(tsvParams.placeholder)

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
                        tsvParams.placeholder = resource!!

                        return false
                    }

                })
                    .override(250, 250)
                    .dontAnimate()
                    .into(imageView)


                if (binding.ivFront.visibility == View.INVISIBLE) binding.ivFront.visibility = View.VISIBLE
            } catch (ex: UninitializedPropertyAccessException) {
                Log.d(TAG, "loadImage: ex " + tsvParams.type)
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

                            loadImage(tsvParamFront,binding.ivFront)

                        }
                        if (tsvParamFront.mEndX - tsvParamFront.mStartX < -3) {
                            tsvParamFront.mImageIndex--
                            if (tsvParamFront.mImageIndex < 0) tsvParamFront.mImageIndex = 0

                            loadImage(tsvParamFront,binding.ivFront)
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

        }

        return super.onTouchEvent(event)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.tv_share,R.id.iv_share-> binding.clShareFront.visibility = View.VISIBLE

            R.id.iv_copy_link, R.id.tv_copy_link -> {
                binding.clShareFront.visibility = View.GONE
                embed(getCode(0))
            }

            R.id.iv_share_link,R.id.tv_share_link -> {
                binding.clShareFront.visibility = View.GONE
                share(getCode(0))
            }



            R.id.tv_go_to_home -> {
                var dashboardIntent = Intent(this, MainDashboardActivity::class.java)
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
        return "<iframe \n" +
                "  src=\"https://www.spyne.ai/shoots/shoot?skuId="+shootId+" \n" +
                "  style=\"border:0; height: 100%; width: 100%;\" framerborder=\"0\"></iframe>"

    }

    private fun showGoToHomeButton(){
        binding.tvShowIframe.visibility = View.VISIBLE
        binding.tvGoToHome.visibility = View.VISIBLE

        binding.tvGoToHome.setOnClickListener(this)
    }
}