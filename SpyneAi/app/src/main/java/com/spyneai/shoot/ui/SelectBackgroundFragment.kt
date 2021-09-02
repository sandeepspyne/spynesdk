package com.spyneai.shoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureEvent
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.WhiteLabelConstants
import com.spyneai.dashboard.ui.enable
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.NewCarBackgroundAdapter
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.utils.log
import kotlinx.android.synthetic.main.activity_credit_plans.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


class SelectBackgroundFragment : BaseFragment<ProcessViewModel,FragmentSelectBackgroundBinding>() {

    val TAG = "SelectBackgroundFragment"

    lateinit var carBackgroundGifList: ArrayList<CarsBackgroundRes.Data>
    var backgroundSelect: String = ""
    lateinit var carbackgroundsAdapter: NewCarBackgroundAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carBackgroundGifList = ArrayList()

        initSelectBackground()

        binding.ivBackGif.setOnClickListener {
            requireActivity().onBackPressed()
        }


        when(getString(R.string.app_name)) {
            AppConstants.KARVI -> {
                binding.cb360.visibility = View.GONE
                binding.tv360.visibility = View.GONE
                binding.tvGenerateGif.text = "Generate Output"
            }
            AppConstants.SWEEP -> {
                binding.tvSample.visibility = View.INVISIBLE
                binding.imageViewGif.visibility = View.INVISIBLE
                binding.cb360.visibility = View.GONE
                binding.tv360.visibility = View.GONE
                binding.tvGenerateGif.text = "Generate Output"
            }
            else -> {
                binding.cb360.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked)
                        binding.tvGenerateGif.text = "Continue"
                    else
                        binding.tvGenerateGif.text = "Generate Output"
                }
            }
        }


        binding.tvGenerateGif.setOnClickListener {
            //update total frame if user clicked interior and misc
            if (viewModel.interiorMiscShootsCount > 0)
                updateTotalFrames()
            else{
                processRequest(true)
            }
        }

        observeTotalFrameUpdate()
        observeProcessSku()

    }

    private fun processRequest(showDialog : Boolean) {
        when(getString(R.string.app_name)) {
            AppConstants.KARVI,AppConstants.SWEEP -> {
                //process image call
                processSku(showDialog)
            }else -> {
            if (binding.cb360.isChecked){
                viewModel.backgroundSelect = backgroundSelect
                viewModel.addRegularShootSummaryFragment.value = true
            }else{
                //process image call
                processSku(showDialog)
            }
            }
        }
    }


    private fun getBackground() {
        val category =
            Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!.toRequestBody(MultipartBody.FORM)

        val authKey =
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)!!.toRequestBody(MultipartBody.FORM)

        viewModel.getBackgroundGifCars(category, authKey)
    }

    private fun initSelectBackground() {

        getBackground()

        viewModel.carGifRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    requireContext().captureEvent(Events.GET_BACKGROUND, Properties())
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.rvBackgroundsCars.visibility = View.VISIBLE
                    binding.tvGenerateGif.enable(true)


                    val response = it.value
                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(response.data[0].gifUrl)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                    backgroundSelect = response.data[0].imageId.toString()

                    for (i in 0..response.data.size-1)
                        (carBackgroundGifList).add(response.data[i])

                    setBackgroundsCar()
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(Events.GET_BACKGROUND_FAILED, Properties(),
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
            carBackgroundGifList as ArrayList<CarsBackgroundRes.Data>, 0,
            object : NewCarBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                    //if (position<carBackgroundList.size)
                    backgroundSelect = carBackgroundGifList[position].imageId.toString()
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

        log("Update Total Frames: "+totalFrames)


        viewModel.updateCarTotalFrames(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.skuId!!,
            totalFrames.toString()
        )
    }

    private fun observeTotalFrameUpdate() {
        viewModel.updateTotalFramesRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    val properties = Properties()
                    properties.apply {
                        this["sku_id"] = viewModel.sku.value?.skuId!!
                        this["total_frames"] = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)
                    }

                    requireContext().captureEvent(Events.TOTAL_FRAMES_UPDATED,properties)
                    processRequest(false)
                }

                is Resource.Failure -> {
                    Utilities.hideProgressDialog()

                    val properties = Properties()
                    properties.apply {
                        this["sku_id"] = viewModel.sku.value?.skuId!!
                        this["total_frames"] = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)
                    }

                    requireContext().captureFailureEvent(
                        Events.TOTAL_FRAMES_UPDATE_FAILED,properties,
                        it.errorMessage!!)

                    handleApiError(it) { updateTotalFrames()}
                }
            }
        })
    }

    private fun processSku(showDialog : Boolean) {
        if (showDialog)
            Utilities.showProgressDialog(requireContext())

        requireContext().captureEvent(
            Events.PROCESS_INITIATED,
            Properties().putValue("sku_id", viewModel.sku.value?.skuId!!)
                .putValue("background_id",backgroundSelect)
        )

        viewModel.processSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.skuId!!,
            backgroundSelect,
        false)

        log("Process sku started")
        log("Auth key: "+Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())
        log("Sku Id: : "+viewModel.sku.value?.skuId!!)
        log("Background Id: : "+backgroundSelect)
    }

    private fun observeProcessSku() {
        viewModel.processSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    //update processed state
                    viewModel.updateIsProcessed(viewModel.sku.value!!.projectId!!,viewModel.sku.value!!.skuId!!)

                    Utilities.hideProgressDialog()

                    requireContext().captureEvent(
                        Events.PROCESS,
                        Properties().putValue("sku_id", viewModel.sku.value?.skuId!!)
                            .putValue("background_id",backgroundSelect)
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
    ) = FragmentSelectBackgroundBinding.inflate(inflater, container, false)

}