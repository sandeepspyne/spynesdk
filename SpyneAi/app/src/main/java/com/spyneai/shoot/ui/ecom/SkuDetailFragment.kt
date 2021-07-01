package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentSkuDetailBinding
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import java.util.ArrayList

class SkuDetailFragment : BaseFragment<ShootViewModel, FragmentSkuDetailBinding>() {

    lateinit var capturedImageAdapter: CapturedImageAdapter
    lateinit var capturedImageList: ArrayList<String>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                    capturedImageList = ArrayList<String>()
                    capturedImageList.clear()
                    for (i in 0..(it.size - 1))
                        (capturedImageList as ArrayList).add(it[i].capturedImage)
                    initCapturedImages()

            }catch (e : Exception){
                e.printStackTrace()
            }
        })

    }

    private fun initCapturedImages(){
        capturedImageAdapter = CapturedImageAdapter(
            requireContext(),
            capturedImageList)

        binding.rvSkuImages.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = capturedImageAdapter
        }


    }



    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuDetailBinding.inflate(inflater, container, false)

}