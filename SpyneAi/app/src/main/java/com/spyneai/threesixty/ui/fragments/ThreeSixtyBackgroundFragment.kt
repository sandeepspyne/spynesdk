package com.spyneai.threesixty.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
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
import com.spyneai.databinding.Fragment360BackgroundBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.NewCarBackgroundAdapter
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ThreeSixtyBackgroundFragment : BaseFragment<ThreeSixtyViewModel, Fragment360BackgroundBinding>() {


    lateinit var carBackgroundGifList: ArrayList<CarsBackgroundRes.Data>
    var backgroundSelect: String = ""
    lateinit var carbackgroundsAdapter: NewCarBackgroundAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carBackgroundGifList = ArrayList()

        initSelectBackground()


        binding.btnContinue.setOnClickListener {

            //set background id
            viewModel.videoDetails.backgroundId = backgroundSelect

            Navigation.findNavController(binding.btnContinue)
                .navigate(R.id.action_threeSixtyBackgroundFragment_to_threeSixtyShootSummaryFragment)
        }
    }



    fun getBackgorund() {
        val category =
            Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!.toRequestBody(
                MultipartBody.FORM)

        val auth_key =
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)!!.toRequestBody(
                MultipartBody.FORM)

        viewModel.getBackgroundGifCars(category, auth_key)
    }

    private fun initSelectBackground() {

        getBackgorund()

        viewModel.carGifRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    requireContext().captureEvent(Events.GET_BACKGROUND, Properties())
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.rvBackgroundsCars.visibility = View.VISIBLE
                    binding.btnContinue.enable(true)


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
                    requireContext().captureFailureEvent(
                        Events.GET_BACKGROUND_FAILED, Properties(),
                        it.errorMessage!!
                    )
                    handleApiError(it) { getBackgorund() }
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


    override fun getViewModel() = ThreeSixtyViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = Fragment360BackgroundBinding.inflate(inflater, container, false)
}