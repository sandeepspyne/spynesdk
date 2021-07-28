package com.spyneai.shoot.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.MotionEventCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentRegularShootSummaryBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.utils.log
import com.spyneai.videorecording.model.TSVParams

class RegularShootSummaryFragment  : BaseFragment<ProcessViewModel, FragmentRegularShootSummaryBinding>(),View.OnTouchListener {

    private var availableCredits = 0
    private lateinit var frontFramesList: List<String>
    lateinit var tsvParamFront : TSVParams
    var handler = Handler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setup360View()

        getUserCredits()
        observeCredits()

        binding.ivBackGif.setOnClickListener { requireActivity().supportFragmentManager
            .beginTransaction()
            .remove(this)
            .commit()
        }

        binding.tvNoOfImages.text = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount).toString()
        binding.tvSkuId.text = viewModel.sku.value?.skuId
        binding.tvTotalImagesClicked.text = viewModel.exteriorAngles.value.toString()
        binding.tvTotalCost.text = viewModel.exteriorAngles.value.toString()

        observeReduceCredits()

        binding.tvGenerateGif.setOnClickListener {
            reduceCredits(true)
        }
    }

    private fun setup360View() {
        frontFramesList = viewModel.frontFramesList

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

                    return false
                }

            })
            .into(binding.ivFront)
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

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {



                        if (index == tsvParams.framesList.size - 1) {

                            //binding.ivFront.setOnTouchListener(this@RegularShootSummaryFragment)
                        }

                        return false
                    }

                })
                .dontAnimate()
                .override(250, 250)
                .preload()

        }

    }



    private fun loadImage(tsvParams: TSVParams, imageView: ImageView) {

        handler.removeCallbacksAndMessages(null)

        handler.postDelayed({


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
                            if (tsvParamFront.mImageIndex >= tsvParamFront.framesList.size) tsvParamFront.mImageIndex = 0

                            loadImage(tsvParamFront,binding.ivFront)

                        }
                        if (tsvParamFront.mEndX - tsvParamFront.mStartX < -3) {
                            tsvParamFront.mImageIndex--
                            if (tsvParamFront.mImageIndex < 0) tsvParamFront.mImageIndex = tsvParamFront.framesList.size - 1

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
        return true
    }

    private fun getUserCredits() {
        viewModel.getUserCredits(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
    }

    private fun reduceCredits(showLoader : Boolean) {
        if (showLoader)
            Utilities.showProgressDialog(requireContext())

        viewModel.reduceCredit(
            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString(),
            viewModel.exteriorAngles.value.toString(),
            WhiteLabelConstants.ENTERPRISE_ID,
            viewModel.sku.value?.skuId!!
        )
    }

    private fun observeReduceCredits() {
        viewModel.reduceCreditResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    processSku(false)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { reduceCredits(true) }
                }
            }
        })
    }

    private fun observeCredits() {
        viewModel.userCreditsRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    availableCredits = it.value.data.credit_available
                    binding.tvCreditsAvailable.text = availableCredits.toString()

                    if (availableCredits >= viewModel.exteriorAngles.value!!)
                        binding.tvGenerateGif.isEnabled = true
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { getUserCredits() }
                }

                is Resource.Loading -> Utilities.showProgressDialog(requireContext())
            }
        })
    }

    private fun processSku(showLoader : Boolean) {
        if (showLoader)
            Utilities.showProgressDialog(requireContext())

        viewModel.processSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.skuId!!,
            viewModel.backgroundSelect!!,
        true)

        log("Process sku started")
        log("Auth key: "+Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())
        log("Sku Id: : "+viewModel.sku.value?.skuId!!)
        log("Background Id: : "+viewModel.backgroundSelect!!)


        viewModel.processSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureEvent(
                        Events.PROCESS,
                        Properties().putValue("sku_id", viewModel.sku.value?.skuId!!)
                            .putValue("background_id",viewModel.backgroundSelect!!)
                    )
                    viewModel.startTimer.value = true
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.PROCESS_FAILED,
                        Properties().putValue("sku_id",viewModel.sku.value?.skuId!!),
                        it.errorMessage!!)

                    handleApiError(it) { processSku(true)}
                }
            }
        })
    }


    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRegularShootSummaryBinding.inflate(inflater, container, false)



}