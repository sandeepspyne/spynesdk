package com.spyneai.shoot.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.spyneai.R
import com.spyneai.adapter.CarBackgroundAdapter
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.databinding.FragmentSelectBackgroundBinding
import com.spyneai.model.carbackgroundgif.CarBackgrounGifResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootViewModel


class SelectBackgroundFragment : BaseFragment<ShootViewModel,FragmentSelectBackgroundBinding>() {

    lateinit var carBackgroundGifList: ArrayList<CarBackgrounGifResponse>
    var backgroundSelect: String = ""
    lateinit var carbackgroundsAdapter: CarBackgroundAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        carBackgroundGifList = ArrayList()

        initSelectBackground()

        binding.tvGenerateGif.setOnClickListener {
            //process image call
            viewModel.checkImagesUploadStatus()

            viewModel.processSku.observe(viewLifecycleOwner,{
                if (it)  processSku()
            })

            viewModel.skuQueued.observe(viewLifecycleOwner,{
                //sku process queued start timer
                viewModel.startTimer.value = true
            })
        }
    }

    private fun initSelectBackground() {
        viewModel.getBackgroundGifCars()

        viewModel.carGifRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Sucess -> {
                    val response = it.value
                    Glide.with(requireContext()) // replace with 'this' if it's in activity
                        .load(response[0].gifUrl)
                        .error(R.mipmap.defaults) // show error drawable if the image is not a gif
                        .into(binding.imageViewGif)

                    backgroundSelect = response[0].imageId.toString()

                    for (i in 0..response.size-1)
                        (carBackgroundGifList).add(response[i])

                    setBackgroundsCar()
                }

                is Resource.Failure -> {

                }

                is Resource.Loading -> {

                }
            }
        })
    }

    private fun setBackgroundsCar() {
        Utilities.showProgressDialog(requireContext())
        carbackgroundsAdapter = CarBackgroundAdapter(requireContext(),
            carBackgroundGifList as ArrayList<CarBackgrounGifResponse>, 0,
            object : CarBackgroundAdapter.BtnClickListener {
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

        viewModel.processSkuRes.observe(viewLifecycleOwner,{
            when(it) {
                is Resource.Loading -> {
                }

                is Resource.Sucess -> {
                    //start timer
                    viewModel.startTimer.value = true
                }
                is Resource.Failure -> {

                }
            }
        })
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectBackgroundBinding.inflate(inflater, container, false)

}