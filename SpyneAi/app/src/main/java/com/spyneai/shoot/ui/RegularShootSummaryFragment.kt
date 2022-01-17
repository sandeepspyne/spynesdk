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
import com.google.gson.Gson
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentRegularShootSummaryBinding
import com.spyneai.fragment.TopUpFragment
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.videorecording.model.TSVParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegularShootSummaryFragment  : BaseFragment<ProcessViewModel, FragmentRegularShootSummaryBinding>(),View.OnTouchListener {

    private var availableCredits = 0
    private lateinit var frontFramesList: List<String>
    lateinit var tsvParamFront : TSVParams
    var handler = Handler()
    var TAG = "RegularShootSummaryFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().setLocale()
        refreshText()

        //setup360View()

//        getUserCredits()
//        observeCredits()

        binding.ivBackGif.setOnClickListener {
            viewModel.isRegularShootSummaryActive = false
            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commit()
        }

        binding.tvNoOfImages.text = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount).toString()
        binding.tvSkuId.text = viewModel.sku?.skuId
        binding.tvTotalImagesClicked.text = viewModel.exteriorAngles.value.toString()
        binding.tvTotalCost.text = viewModel.exteriorAngles.value.toString()



        binding.tvGenerateGif.setOnClickListener {
            processSku()
        }

        when(getString(R.string.app_name)){
            "Yalla Motors","Travo Photos" -> binding.tvTopUp.visibility = View.GONE

            else-> {
                binding.tvTopUp.setOnClickListener {
                    TopUpFragment().show(requireActivity().supportFragmentManager,"TopUpFragment")
                }
            }
        }

    }

    private fun observeProcessSku() {
        viewModel.processSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureEvent(
                        Events.PROCESS,
                        HashMap<String,Any?>()
                            .apply {
                                this.put("sku_id", viewModel.sku?.skuId!!)
                                this.put("background_id",viewModel.backgroundSelect!!)
                                this.put("response", Gson().toJson(it).toString())
                            }


                    )
                    Log.d(TAG, "observeProcessSku: ")
                    viewModel.startTimer.value = true
                }
                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    requireContext().captureFailureEvent(
                        Events.PROCESS_FAILED,
                        HashMap<String,Any?>()
                            .apply {
                                this.put("sku_id",viewModel.sku?.skuId!!)
                                this.put("throwable", it.throwable)
                            },
                        it.errorMessage!!)

                    handleApiError(it) { processSku()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.isRegularShootSummaryActive = true
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

    fun refreshText(){
        requireContext().setLocale()
        binding.tvCategoryName.text = getString(R.string.automobile)
        binding.tvCat.text = getString(R.string.category)
        binding.tvNoOfImg.text = getString(R.string.images)
        binding.tvTotalExteriorImages.text = getString(R.string.total_exterior_clicked)
        binding.tvTotalExteriorUnit.text = getString(R.string.images)
        binding.tvCreditsUnit.text = getString(R.string.credits)
        binding.tvCredits.text = getString(R.string.credits)
        binding.tvCreditAvailable.text=getString(R.string.credits_available)
        binding.tvCost.text=getString(R.string.total_cost)
        binding.tvTopUp.text=getString(R.string.top_up_2)
        binding.tvShootSummary.text=getString(R.string.shoot_summary)

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
        Utilities.showProgressDialog(requireContext())
        viewModel.getUserCredits(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
    }

    private fun reduceCredits(showLoader : Boolean) {
        if (showLoader)
            Utilities.showProgressDialog(requireContext())

        viewModel.reduceCredit(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.exteriorAngles.value.toString(),
            viewModel.sku?.skuId.toString()
        )
    }

    private fun observeReduceCredits() {
        viewModel.reduceCreditResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    updatePaidStatus(false)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { reduceCredits(true) }
                }
            }
        })
    }

    private fun updatePaidStatus(showLoader : Boolean) {
        if (showLoader)
            Utilities.showProgressDialog(requireContext())

        viewModel.updateDownloadStatus(
            Utilities.getPreference(requireContext(), AppConstants.TOKEN_ID).toString(),
            viewModel.sku?.skuId!!,
            WhiteLabelConstants.ENTERPRISE_ID,
            true
        )
    }

    private fun observepaidStatus() {
        viewModel.downloadHDRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    processSku()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    handleApiError(it) { updatePaidStatus(true) }
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
            }
        })
    }

    private fun processSku() {
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateBackground()
        }


        Utilities.hideProgressDialog()

        requireContext().captureEvent(
            Events.PROCESS,
            HashMap<String, Any?>()
                .apply {
                    this.put("sku_id", viewModel.sku?.uuid!!)
                    this.put("background_id", viewModel.backgroundSelect)
                }


        )

        viewModel.startTimer.value = true

        //start sync service
        requireContext().startUploadingService(
            RegularShootSummaryFragment::class.java.simpleName,
            ServerSyncTypes.PROCESS
        )
    }

    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRegularShootSummaryBinding.inflate(inflater, container, false)



}