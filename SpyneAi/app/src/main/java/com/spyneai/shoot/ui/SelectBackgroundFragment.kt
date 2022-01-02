package com.spyneai.shoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.NewCarBackgroundAdapter
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.model.CarsBackgroundRes
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


class SelectBackgroundFragment : BaseFragment<ProcessViewModel, FragmentSelectBackgroundBinding>() {

    val TAG = "Background Fragment"

    lateinit var carBackgroundGifList: ArrayList<CarsBackgroundRes.Background>
    var backgroundSelect: String = ""
    lateinit var carbackgroundsAdapter: NewCarBackgroundAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carBackgroundGifList = ArrayList()

        initSelectBackground()

        if (viewModel.categoryId == null) {
            arguments?.let {
                viewModel.categoryId = it.getString(AppConstants.CATEGORY_ID)
            }
        }

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.cbBlurNoPlate.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.numberPlateBlur = isChecked
        }
        binding.cbWindowCorrection.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.windowCorrection = isChecked
        }
        binding.cbTintWindow.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.tintWindow = isChecked
        }

        when (viewModel.categoryId) {
            AppConstants.CARS_CATEGORY_ID -> {
                when(getString(R.string.app_name)) {
                    AppConstants.SPYNE_AI,
                    AppConstants.ADLOID-> {
                        binding.cbWindowCorrection.visibility = View.GONE
                        binding.tvWindowReflection.visibility = View.GONE

                        binding.cbBlurNoPlate.visibility = View.VISIBLE
                        binding.tvBlurNoPlate.visibility = View.VISIBLE

                        binding.tvTintWindow.visibility = View.VISIBLE
                        binding.cbTintWindow.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.cbWindowCorrection.visibility = View.GONE
                        binding.tvWindowReflection.visibility = View.GONE

                        binding.cbBlurNoPlate.visibility = View.GONE
                        binding.tvBlurNoPlate.visibility = View.GONE

                        binding.tvTintWindow.visibility = View.VISIBLE
                        binding.cbTintWindow.visibility = View.VISIBLE
                    }
                }
            }
            else -> {
                binding.cbBlurNoPlate.visibility = View.GONE
                binding.tvBlurNoPlate.visibility = View.GONE

                binding.cbWindowCorrection.visibility = View.GONE
                binding.tvWindowReflection.visibility = View.GONE

                binding.cbTintWindow.visibility = View.GONE
                binding.tvTintWindow.visibility = View.GONE
            }
        }

        when (getString(R.string.app_name)) {
            AppConstants.KARVI,
            AppConstants.CARS24_INDIA,
            AppConstants.CARS24 -> {
                binding.cb360.visibility = View.GONE
                binding.tv360.visibility = View.GONE
                binding.tvGenerateGif.text = getString(R.string.generate_output)
            }

            AppConstants.SWEEP -> {
                binding.tvSample.visibility = View.INVISIBLE
                binding.imageViewGif.visibility = View.INVISIBLE
                binding.cb360.visibility = View.GONE
                binding.tv360.visibility = View.GONE
                binding.tvGenerateGif.text = getString(R.string.generate_output)
            }
            AppConstants.SWIGGY -> {
                binding.cb360.visibility = View.GONE
                binding.tv360.visibility = View.GONE
                binding.tvGenerateGif.text = getString(R.string.generate_output)
                binding.tvSample.text = getString(R.string.sample_output)
            }
            AppConstants.SPYNE_AI -> {
                when (viewModel.categoryId) {
                    AppConstants.FOOD_AND_BEV_CATEGORY_ID -> {
                        binding.cb360.visibility = View.GONE
                        binding.tv360.visibility = View.GONE
                        binding.tvGenerateGif.text = getString(R.string.generate_output)
                        binding.tvSample.text = getString(R.string.sample_output)
                    }

                    AppConstants.BIKES_CATEGORY_ID -> {
                        binding.cb360.visibility = View.GONE
                        binding.tv360.visibility = View.GONE
                        binding.tvGenerateGif.text = getString(R.string.generate_output)
                    }

                    else -> {
                        binding.cb360.setOnCheckedChangeListener { buttonView, isChecked ->
                            if (isChecked)
                                binding.tvGenerateGif.text = getString(R.string.contiune)
                            else
                                binding.tvGenerateGif.text = getString(R.string.generate_output)
                        }
                    }
                }
            }
            else -> {
                binding.cb360.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked)
                        binding.tvGenerateGif.text = getString(R.string.contiune)
                    else
                        binding.tvGenerateGif.text = getString(R.string.generate_output)
                }
            }
        }

        if (viewModel.fromVideo) {
            binding.cb360.visibility = View.GONE
            binding.tv360.visibility = View.GONE
            binding.tvGenerateGif.text = getString(R.string.generate_output)
        }



        binding.tvGenerateGif.setOnClickListener {
            //update total frame if user clicked interior and misc
            if (viewModel.interiorMiscShootsCount > 0)
                updateTotalFrames()
            else {
                processRequest(true)
            }
        }

        observeTotalFrameUpdate()
        observeProcessSku()
    }

    private fun processRequest(showDialog: Boolean) {
        when (getString(R.string.app_name)) {
            AppConstants.KARVI,
            AppConstants.SWEEP,
            AppConstants.CARS24,
            AppConstants.CARS24_INDIA -> {
                //process image call
                processSku(showDialog)
            }
            AppConstants.SWIGGY -> {

                processFoodImage()
                Utilities.showProgressDialog(requireContext())
            }
            AppConstants.SPYNE_AI -> {
                if (Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)
                        .equals("Food & Beverages")
                ) {
                    processFoodImage()
                    Utilities.showProgressDialog(requireContext())
                } else {
                    if (binding.cb360.isChecked) {
                        viewModel.backgroundSelect = backgroundSelect
                        viewModel.addRegularShootSummaryFragment.value = true
                    } else {
                        //process image call
                        processSku(showDialog)
                    }
                }
            }
            else -> {
                if (binding.cb360.visibility == View.VISIBLE && binding.cb360.isChecked) {
                    viewModel.backgroundSelect = backgroundSelect
                    viewModel.addRegularShootSummaryFragment.value = true
                } else {
                    //process image call
                    processSku(showDialog)
                }
            }
        }

        observeProcessSku()
        observeFoodProcess()
    }

    private fun processFoodImage() {
        binding.shimmer.startShimmer()

        viewModel.skuProcessStateWithBackgroundid(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            arguments?.getString(AppConstants.PROJECT_ID)!!, backgroundSelect.toInt()
        )
    }

    private fun observeFoodProcess() {
        viewModel.skuProcessStateWithBgResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    Utilities.hideProgressDialog()
                    requireContext().gotoHome()
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()
                    binding.shimmer.stopShimmer()
                    requireContext().captureFailureEvent(
                        Events.GET_BACKGROUND_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it) { processFoodImage() }
                }
            }
        })
    }

    private fun getBackground() {

        when (getString(R.string.app_name)) {
            AppConstants.SWIGGY -> {
                viewModel.getBackgroundGifCars("Food")
            }
            AppConstants.SPYNE_AI -> {
                if (Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)
                        .equals("Food & Beverages")
                ) {
                    viewModel.getBackgroundGifCars("Food")
                } else {
                    viewModel.getBackgroundGifCars(Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!)
                }
            }
            else -> {
                viewModel.getBackgroundGifCars(Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!)
            }
        }
    }

    private fun initSelectBackground() {

        getBackground()

        viewModel.carGifRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    requireContext().captureEvent(Events.GET_BACKGROUND, HashMap<String, Any?>())
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.rvBackgroundsCars.visibility = View.VISIBLE
                    binding.tvGenerateGif.enable(true)

                    val response = it.value
                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(response.data[0].gifUrl)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                    viewModel.backgroundSelect = response.data[0].imageId
                    backgroundSelect = response.data[0].imageId
                    viewModel.backgroundSelect = backgroundSelect

                    carBackgroundGifList.clear()
                    for (element in response.data) {
                        carBackgroundGifList.add(element)
                    }


                    setBackgroundsCar()
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_BACKGROUND_FAILED, HashMap<String, Any?>(),
                        it.errorMessage!!
                    )
                    handleApiError(it) { getBackground() }
                }

                is Resource.Loading -> binding.shimmer.startShimmer()
            }
        })
    }

    private fun setBackgroundsCar() {
        carbackgroundsAdapter = NewCarBackgroundAdapter(requireContext(),
            carBackgroundGifList, 0,
            object : NewCarBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    //if (position<carBackgroundList.size)
                    viewModel.backgroundSelect = backgroundSelect
                    backgroundSelect = carBackgroundGifList[position].imageId
                    viewModel.backgroundSelect = backgroundSelect
                    carbackgroundsAdapter.notifyDataSetChanged()

                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(carBackgroundGifList[position].gifUrl)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                    //showPreviewCar()
                }
            })
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false
            )

        binding.rvBackgroundsCars.setLayoutManager(layoutManager)
        binding.rvBackgroundsCars.setAdapter(carbackgroundsAdapter)
    }


    private fun updateTotalFrames() {
        Utilities.showProgressDialog(requireContext())
        val totalFrames = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)

        Log.d(TAG, "updateTotalFrames: " + viewModel.exteriorAngles.value)


        viewModel.updateCarTotalFrames(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.sku?.skuId!!,
            totalFrames.toString()
        )
    }

    private fun observeTotalFrameUpdate() {
        viewModel.updateTotalFramesRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    Utilities.hideProgressDialog()

                    val properties = HashMap<String, Any?>()
                    properties.apply {
                        this["sku_id"] = viewModel.sku?.skuId!!
                        this["total_frames"] =
                            viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)
                    }

                    requireContext().captureEvent(Events.TOTAL_FRAMES_UPDATED, properties)
                    processRequest(false)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    val properties = HashMap<String, Any?>()
                    properties.apply {
                        this["sku_id"] = viewModel.sku?.skuId!!
                        this["total_frames"] =
                            viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)
                    }

                    requireContext().captureFailureEvent(
                        Events.TOTAL_FRAMES_UPDATE_FAILED, properties,
                        it.errorMessage!!
                    )

                    handleApiError(it) { updateTotalFrames() }
                }
            }
        })
    }

    private fun processSku(showDialog: Boolean) {
        if (!viewModel.backgroundSelect.isNullOrEmpty()) {
            if (showDialog)
                Utilities.showProgressDialog(requireContext())

            requireContext().captureEvent(
                Events.PROCESS_INITIATED,
                HashMap<String, Any?>()
                    .apply {
                        this.put("sku_id", viewModel.sku?.skuId!!)
                        this.put("background_id", backgroundSelect)
                    }


            )

            viewModel.processSku(
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
                viewModel.sku?.skuId!!,
                backgroundSelect,
                false,
                viewModel.numberPlateBlur,
                viewModel.windowCorrection,
                viewModel.tintWindow
            )
        }
    }


    private fun observeProcessSku() {
        viewModel.processSkuRes.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Success -> {
                    //update processed state
                    viewModel.updateIsProcessed(
                        viewModel.sku!!.projectId!!,
                        viewModel.sku!!.skuId!!
                    )

                    Utilities.hideProgressDialog()

                    requireContext().captureEvent(
                        Events.PROCESS,
                        HashMap<String, Any?>()
                            .apply {
                                this.put("sku_id", viewModel.sku?.skuId!!)
                                this.put("background_id", backgroundSelect)
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
                        HashMap<String, Any?>().apply {
                            this.put("sku_id", viewModel.sku?.skuId!!)
                            this.put("throwable", it.throwable)
                        },
                        it.errorMessage!!
                    )

                    handleApiError(it) { processSku(true) }
                }
            }
        })
    }

    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectBackgroundBinding.inflate(inflater, container, false)

}