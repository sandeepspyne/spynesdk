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
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


class SelectBackgroundFragment : BaseFragment<ProcessViewModel,FragmentSelectBackgroundBinding>() {

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

        binding.tvGenerateGif.setOnClickListener {
            //process image call
            processSku()
        }
    }

    private fun initSelectBackground() {

        val category =
            Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!.toRequestBody(MultipartBody.FORM)

        val auth_key =
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)!!.toRequestBody(MultipartBody.FORM)

        viewModel.getBackgroundGifCars(category, auth_key)

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
                    handleApiError(it)
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

    private fun processSku() {
        viewModel.processSku(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
        viewModel.sku.value?.skuId!!,
        backgroundSelect)

        log("Process sku started")
        log("Auth key: "+Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString())
        log("Sku Id: : "+viewModel.sku.value?.skuId!!)
        log("Background Id: : "+backgroundSelect)


        viewModel.processSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Loading -> {
                }

                is Resource.Success -> {
                    requireContext().captureEvent(
                        Events.PROCESS,
                        Properties().putValue("sku_id", viewModel.sku.value?.skuId!!)
                            .putValue("background_id",backgroundSelect)
                    )
                    viewModel.startTimer.value = true
                }
                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.PROCESS_FAILED,
                        Properties().putValue("sku_id",viewModel.sku.value?.skuId!!),
                        it.errorMessage!!)
                    handleApiError(it)
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