package com.spyneai.shoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.spyneai.gotoHome
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.FoodBackgroundAdapter
import com.spyneai.shoot.adapters.NewCarBackgroundAdapter
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.model.CarsBackgroundRes
import com.spyneai.shoot.utils.log
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody


class SelectBackgroundFragment : BaseFragment<ProcessViewModel,FragmentSelectBackgroundBinding>() {

    lateinit var carBackgroundGifList: ArrayList<CarsBackgroundRes.Data>
   lateinit var foodBackgroundList : ArrayList<String>
    var backgroundSelect: String = ""
    lateinit var carbackgroundsAdapter: NewCarBackgroundAdapter
    lateinit var foodBackgroundAdapter: FoodBackgroundAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carBackgroundGifList = ArrayList()
        foodBackgroundList = ArrayList()
        foodBackgroundList.add("https://storage.googleapis.com/spyne/AI/raw/1892526c-72c9-4331-8ede-dec5f72cf52e.png")
        foodBackgroundList.add("https://storage.googleapis.com/spyne/AI/raw/5a2f8ef2-ecd8-4d35-b747-fcb1b08223cd.jpg")
        foodBackgroundList.add("https://storage.googleapis.com/spyne/AI/raw/72cbc0ea-3373-4312-99ba-4ef69de4384e.jpg")
        foodBackgroundList.add("https://storage.googleapis.com/spyne/AI/raw/72cbc0ea-3373-4312-99ba-4ef69de4384e.jpg")
        foodBackgroundList.add("https://storage.googleapis.com/spyne/AI/raw/72cbc0ea-3373-4312-99ba-4ef69de4384e.jpg")

//        when(getString(R.string.app_name)) {
//            AppConstants.SWIGGY ->
//                setBackgroundFood()
//             else ->
//                 initSelectBackground()
//        }

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
            AppConstants.SWIGGY -> {
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

            when(getString(R.string.app_name)) {
                AppConstants.KARVI,AppConstants.SWEEP -> {
                    //process image call
                    processSku()
                }
                AppConstants.SWIGGY -> {
                   processFoodImage()
                }
                else -> {
                if (binding.cb360.isChecked){
                    viewModel.backgroundSelect = backgroundSelect
                    viewModel.addRegularShootSummaryFragment.value = true
                }else{
                    //process image call
                    processSku()
                }
                }
            }
        }


        observeProcessSku()
        }

    private fun processFoodImage(){
        viewModel.skuProcessStateWithBackgroundid(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.projectId.value.toString(), backgroundSelect)
        log("auth key- "+Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
        log("project id- "+viewModel.projectId.value)
        log("skuProcessState called")

        viewModel.skuProcessStateWithBgResponse.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "skuProcessState sucess", Toast.LENGTH_SHORT).show()
                    Utilities.hideProgressDialog()
                    requireContext().gotoHome()
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

    private fun setBackgroundFood(){
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
        foodBackgroundAdapter = FoodBackgroundAdapter(requireContext(),
            foodBackgroundList , 0,
            object : FoodBackgroundAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    Log.e("position preview", position.toString())
                    //if (position<carBackgroundList.size)
                    when(position){
                        0 ->{
                            backgroundSelect = "111"
                            foodBackgroundAdapter.notifyDataSetChanged()
                            Glide.with(requireContext()) // replace with 'this' if it's in activity
                                .load("https://storage.googleapis.com/spyne/AI/raw/9261811d-d93f-4f10-9cbb-f20b95aff18e.png")
                                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                                .into(binding.imageViewGif)
                            Toast.makeText(requireContext(), "backgroundID" +backgroundSelect, Toast.LENGTH_SHORT).show()
                        }
                            1 -> {
                                backgroundSelect = "222"
                                foodBackgroundAdapter.notifyDataSetChanged()
                                Glide.with(requireContext()) // replace with 'this' if it's in activity
                                    .load("https://storage.googleapis.com/spyne/AI/raw/84292221-14ec-4d5d-88ea-1cf96030454c.png")
                                    .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                                    .into(binding.imageViewGif)
                                Toast.makeText(requireContext(), "backgroundID" +backgroundSelect, Toast.LENGTH_SHORT).show()
                            }
                        2 -> {
                            backgroundSelect = "333"
                            foodBackgroundAdapter.notifyDataSetChanged()
                            Glide.with(requireContext()) // replace with 'this' if it's in activity
                                .load("https://storage.googleapis.com/spyne/AI/raw/f2ef1db7-500b-4b05-8bd5-6645fe1dc2b1.png")
                                .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                                .into(binding.imageViewGif)
                            Toast.makeText(requireContext(), "backgroundID" +backgroundSelect, Toast.LENGTH_SHORT).show()
                        } else -> {
                        backgroundSelect = "444"
                        foodBackgroundAdapter.notifyDataSetChanged()
                        Glide.with(requireContext()) // replace with 'this' if it's in activity
                            .load("https://storage.googleapis.com/spyne/AI/raw/9261811d-d93f-4f10-9cbb-f20b95aff18e.png")
                            .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                            .into(binding.imageViewGif)
                        Toast.makeText(requireContext(), "backgroundID" +backgroundSelect, Toast.LENGTH_SHORT).show()
                        }
                    }

                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load("https://storage.googleapis.com/spyne/AI/raw/9261811d-d93f-4f10-9cbb-f20b95aff18e.png")
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
        binding.rvBackgroundsCars.setAdapter(foodBackgroundAdapter)
    }

    private fun getBackground() {

        when(getString(R.string.app_name)) {
        AppConstants.SWIGGY -> {
            Toast.makeText(requireContext(), "Get background Food", Toast.LENGTH_SHORT).show()
            val category = "Food".toRequestBody(MultipartBody.FORM)
            val authKey =
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)!!.toRequestBody(MultipartBody.FORM)

            viewModel.getBackgroundGifCars(category, authKey)
        }   else -> {
            val category = Utilities.getPreference(requireContext(), AppConstants.CATEGORY_NAME)!!.toRequestBody(MultipartBody.FORM)
            val authKey =
                Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY)!!.toRequestBody(MultipartBody.FORM)

            viewModel.getBackgroundGifCars(category, authKey)
        }
        }
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
        val totalFrames = viewModel.exteriorAngles.value?.plus(viewModel.interiorMiscShootsCount)

        viewModel.updateCarTotalFrames(
            Utilities.getPreference(requireContext(),AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.skuId!!,
            totalFrames.toString()
        )
    }

    private fun processSku() {
        requireContext().captureEvent(
            Events.PROCESS_INITIATED,
            Properties().putValue("sku_id", viewModel.sku.value?.skuId!!)
                .putValue("background_id",backgroundSelect)
        )

        Utilities.showProgressDialog(requireContext())

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

                    handleApiError(it) { processSku()}
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