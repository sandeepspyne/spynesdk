package com.spyneai.reshoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.databinding.FragmentReshootBinding
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.reshoot.ReshootAdapter

class ReshootFragment : BaseFragment<ProcessedViewModel,FragmentReshootBinding>(),OnItemClickListener{

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()
    }

    private fun getImages() {
        val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value

        binding.rvSkuImages.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = ReshootAdapter(imagesResponse.data,this@ReshootFragment)
        }

    }

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReshootBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {

    }
}