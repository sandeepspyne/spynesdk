package com.spyneai.threesixty.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
import com.spyneai.service.Actions
import com.spyneai.service.getServiceState
import com.spyneai.service.log
import com.spyneai.shoot.adapters.NewCarBackgroundAdapter
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.ui.base.ShootActivity
import com.spyneai.startVideoUploadService
import com.spyneai.threesixty.data.ThreeSixtyViewModel
import com.spyneai.threesixty.data.VideoUploadService
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ThreeSixtyBackgroundFragment : BaseFragment<ThreeSixtyViewModel, Fragment360BackgroundBinding>() {


    lateinit var carBackgroundGifList: ArrayList<CarsBackgroundRes.Background>
    var backgroundSelect: String = ""
    lateinit var carbackgroundsAdapter: NewCarBackgroundAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carBackgroundGifList = ArrayList()

        initSelectBackground()

       when(getString(R.string.app_name)){
           AppConstants.OLA_CABS,AppConstants.SPYNE_AI -> {
               binding.apply {
                   tvContinueShoot.visibility = View.VISIBLE
                   llOr.visibility = View.VISIBLE
                   flShootNow.visibility = View.VISIBLE
               }

               binding.flShootNow.setOnClickListener{

                   requireContext().startVideoUploadService()

                   val intent = Intent(requireContext(), ShootActivity::class.java)

                   intent.apply {
                       putExtra(AppConstants.CATEGORY_NAME, "Automobiles")
                       putExtra(AppConstants.CATEGORY_ID, AppConstants.CARS_CATEGORY_ID)
                       putExtra(AppConstants.PROJECT_UUIID, viewModel.videoDetails?.projectUuid)
                       putExtra(AppConstants.PROJECT_ID, viewModel.videoDetails?.projectId)
                       putExtra(AppConstants.SKU_UUID, viewModel.videoDetails?.skuUuid)
                       putExtra(AppConstants.SKU_ID, viewModel.videoDetails?.skuId)
                       putExtra(AppConstants.SKU_NAME, viewModel.videoDetails?.skuName)
                       putExtra(AppConstants.TOTAL_FRAME, viewModel.videoDetails?.frames)
                       putExtra(AppConstants.FROM_VIDEO, true)
                   }

                   startActivity(intent)
               }
           }else -> {

           }
       }

        binding.btnContinue.setOnClickListener {

            //set background id
            viewModel.videoDetails?.backgroundId = backgroundSelect

            Navigation.findNavController(binding.btnContinue)
                .navigate(R.id.action_threeSixtyBackgroundFragment_to_threeSixtyShootSummaryFragment)

            viewModel.title.value = "Shoot Summary"
        }
    }


    fun getBackgorund() {
        viewModel.getBackgroundGifCars(viewModel.videoDetails?.categoryName!!)
    }

    private fun initSelectBackground() {

        getBackgorund()

        viewModel.carGifRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    requireContext().captureEvent(Events.GET_BACKGROUND, HashMap<String,Any?>())
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.rvBackgroundsCars.visibility = View.VISIBLE
                    binding.btnContinue.enable(true)


                    val response = it.value

                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(response.data[0].gifUrl)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                    viewModel.videoDetails?.sample360 = response.data[0].gifUrl
                    viewModel.videoDetails?.backgroundId = response.data[0].imageId
                    viewModel.videoDetails?.bgName = response.data[0].bgName

                    backgroundSelect = response.data[0].imageId

                    for (i in 0..response.data.size-1)
                        (carBackgroundGifList).add(response.data[i])

                    setBackgroundsCar()
                }

                is Resource.Failure -> {
                    requireContext().captureFailureEvent(
                        Events.GET_BACKGROUND_FAILED, HashMap<String,Any?>(),
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
            carBackgroundGifList as ArrayList<CarsBackgroundRes.Background>, 0,
            object : NewCarBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                    //if (position<carBackgroundList.size)
                    backgroundSelect = carBackgroundGifList[position].imageId.toString()
                    carbackgroundsAdapter.notifyDataSetChanged()

                    viewModel.videoDetails?.sample360 = carBackgroundGifList[position].gifUrl
                    viewModel.videoDetails?.backgroundId = carBackgroundGifList[position].imageId
                    viewModel.videoDetails?.bgName = carBackgroundGifList[position].bgName

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