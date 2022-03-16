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
import com.spyneai.*
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.service.ServerSyncTypes
import com.spyneai.shoot.adapters.NewCarBackgroundAdapter
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.model.CarsBackgroundRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
                val projectUuid = it.getString(AppConstants.PROJECT_UUIID)!!
                val skuUUid = it.getString(AppConstants.SKU_UUID)!!

                GlobalScope.launch(Dispatchers.IO) {
                    viewModel.setProjectAndSkuData(
                        projectUuid,
                        skuUUid
                    )
                }
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
                processRequest()
            }
        }

    }

    private fun processRequest() {
        when (getString(R.string.app_name)) {
            AppConstants.KARVI,
            AppConstants.SWEEP,
            AppConstants.CARS24,
            AppConstants.CARS24_INDIA -> {
                //process image call
                processSku(false)
            }
            AppConstants.SWIGGY -> {
//                processFoodImage()
//                Utilities.showProgressDialog(requireContext())
                processSku(true)
            }
            AppConstants.SPYNE_AI -> {
                viewModel.interiorMiscShootsCount = viewModel.sku?.imagesCount!!

                if (Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)
                        .equals("Food & Beverages")
                ) {
//                    processFoodImage()
//                    Utilities.showProgressDialog(requireContext())

                    processSku(true)
                } else {
                    if (binding.cb360.isChecked) {
                        viewModel.backgroundSelect = backgroundSelect
                        viewModel.addRegularShootSummaryFragment.value = true
                    } else {
                        //process image call
                        processSku(false)
                    }
                }
            }
            else -> {
                if (binding.cb360.visibility == View.VISIBLE && binding.cb360.isChecked) {
                    viewModel.backgroundSelect = backgroundSelect
                    viewModel.addRegularShootSummaryFragment.value = true
                } else {
                    //process image call
                    processSku(false)
                }
            }
        }

    }

    private fun getBackground() {
        when(viewModel.categoryId){
            AppConstants.CARS_CATEGORY_ID -> viewModel.getBackgroundGifCars("Automobiles")
            AppConstants.FOOD_AND_BEV_CATEGORY_ID -> viewModel.getBackgroundGifCars("Food")
        }
    }

    private fun initSelectBackground() {

        getBackground()

        viewModel.carGifRes.observe(viewLifecycleOwner) {
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
                    viewModel.bgName = response.data[0].bgName

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
        }
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
                    viewModel.bgName = carBackgroundGifList[position].bgName
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
        processRequest()
    }



    private fun processSku(gotoHome: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.updateBackground()

            GlobalScope.launch(Dispatchers.Main) {
                Utilities.hideProgressDialog()

                requireContext().captureEvent(
                    Events.PROCESS,
                    HashMap<String, Any?>()
                        .apply {
                            this.put("sku_id", viewModel.sku?.uuid!!)
                            this.put("background_id", backgroundSelect)
                        }
                )

                //start sync service
                requireContext().startUploadingService(
                    SelectBackgroundFragment::class.java.simpleName,
                    ServerSyncTypes.PROCESS
                )

                if (gotoHome)
                    requireContext().gotoHome()
                else
                    viewModel.startTimer.value = true
            }
        }
    }



    override fun getViewModel() = ProcessViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectBackgroundBinding.inflate(inflater, container, false)

}