package com.spyneai.reshoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.camera2.OverlaysResponse
import com.spyneai.dashboard.ui.enable
import com.spyneai.databinding.FragmentReshootBinding
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.reshoot.ReshootAdapter
import com.spyneai.shoot.ui.dialogs.ReclickDialog

class ReshootFragment : BaseFragment<ProcessedViewModel,FragmentReshootBinding>(),OnItemClickListener{

    private var reshootAdapter : ReshootAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()
    }

    private fun getImages() {
        val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value

        reshootAdapter = ReshootAdapter(imagesResponse.data,this)

        binding.rvSkuImages.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = reshootAdapter
        }

    }

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReshootBinding.inflate(inflater, container, false)

    override fun onItemClick(view: View, position: Int, data: Any?) {
       when(data){
           is ImagesOfSkuRes.Data -> {
               data.isSelected = !data.isSelected
               reshootAdapter?.notifyItemChanged(position)

               val list = reshootAdapter?.listItems as ArrayList<ImagesOfSkuRes.Data>

               val selectedList = list.filter {
                   it.isSelected == true
               }

               if (selectedList.isNullOrEmpty()){
                   binding.btnReshoot.text = getString(R.string.no_reshoot)
                   binding.btnReshoot.enable(false)
               }else {
                   binding.btnReshoot.text = getString(R.string.no_reshoot)+" "+selectedList.size+" Angles"
                   binding.btnReshoot.enable(true)
               }

           }
       }
    }
}