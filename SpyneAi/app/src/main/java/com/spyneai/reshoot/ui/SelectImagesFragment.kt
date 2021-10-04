package com.spyneai.reshoot.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.OnItemClickListener
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.enable
import com.spyneai.databinding.FragmentSelectImagesBinding
import com.spyneai.needs.AppConstants
import com.spyneai.orders.data.response.ImagesOfSkuRes
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.reshoot.SelectImageAdapter
import com.spyneai.reshoot.data.SelectedImagesHelper

class SelectImagesFragment : BaseFragment<ProcessedViewModel,FragmentSelectImagesBinding>(),OnItemClickListener{

    private var selectImageAdapter : SelectImageAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getImages()

        binding.btnReshoot.setOnClickListener {
            val list = selectImageAdapter?.listItems as ArrayList<ImagesOfSkuRes.Data>

            val selectedList = list.filter {
                it.isSelected == true
            }

            selectedList.forEachIndexed { index, data ->
                if (index != 0)
                    data.isSelected = false
            }

            SelectedImagesHelper.selectedImages = selectedList as ArrayList<ImagesOfSkuRes.Data>

            val reshootIntent = Intent(requireActivity(),ReshootActivity::class.java)
            reshootIntent.apply {
                putExtra(AppConstants.PROJECT_ID,viewModel.projectId)
                putExtra(AppConstants.SKU_ID,viewModel.skuId)
                putExtra(AppConstants.SKU_NAME,viewModel.skuName)
                putExtra(AppConstants.CATEGORY_ID,requireActivity().intent.getStringExtra(AppConstants.CATEGORY_ID))
                putExtra(AppConstants.CATEGORY_NAME,requireActivity().intent.getStringExtra(AppConstants.CATEGORY_NAME))
                putExtra(AppConstants.SUB_CAT_ID,requireActivity().intent.getStringExtra(AppConstants.SUB_CAT_ID))
                putExtra(AppConstants.EXTERIOR_ANGLES,requireActivity().intent.getIntExtra(AppConstants.EXTERIOR_ANGLES,0))
                startActivity(this)
            }

        }
    }

    private fun getImages() {
        val imagesResponse = (viewModel.imagesOfSkuRes.value as Resource.Success).value

        selectImageAdapter = SelectImageAdapter(imagesResponse.data,this)

        binding.rvSkuImages.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter = selectImageAdapter
        }

    }

    override fun onItemClick(view: View, position: Int, data: Any?) {
        when(data){
            is ImagesOfSkuRes.Data -> {
                data.isSelected = !data.isSelected
                selectImageAdapter?.notifyItemChanged(position)

                val list = selectImageAdapter?.listItems as ArrayList<ImagesOfSkuRes.Data>

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

    override fun getViewModel() = ProcessedViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectImagesBinding.inflate(inflater, container, false)


}