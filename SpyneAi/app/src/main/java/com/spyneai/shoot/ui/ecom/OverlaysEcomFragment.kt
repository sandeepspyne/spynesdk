package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentOverlaysEcomBinding
import com.spyneai.needs.Utilities
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.utils.log
import java.util.ArrayList

class OverlaysEcomFragment : BaseFragment<ShootViewModel, FragmentOverlaysEcomBinding>() {

    private var showDialog = true
    lateinit var capturedImageAdapter: CapturedImageAdapter
    lateinit var capturedImageList: ArrayList<String>
    var position = 1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (viewModel.projectId.value == null){
            initProjectDialog()
            log("project and SKU dialog shown")
        }

        else {
            initSkuDialog()
            log("SKU dialog shown")
        }

        binding.ivEndProject.setOnClickListener {
            if (viewModel.isStopCaptureClickable)
            viewModel.stopShoot.value = true
        }


        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (showDialog && !it.isNullOrEmpty()) {
                    capturedImageList = ArrayList<String>()
                    position = it.size - 1
                    capturedImageList.clear()
                    for (i in 0..(it.size - 1))
                        (capturedImageList as ArrayList).add(it[i].capturedImage)
                    initCapturedImages()
                    showImageConfirmDialog(it.get(it.size - 1))
                    log("call showImageConfirmDialog")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        // set sku name
        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
            if (it) {
                binding.tvSkuName?.text = viewModel.sku.value?.skuName
                binding.tvSkuName.visibility = View.VISIBLE
                log("sku name set to text view: "+viewModel.sku.value?.skuName)
                viewModel.isSkuCreated.value = false
            }
        })

        viewModel.reshootCapturedImage.observe(viewLifecycleOwner,{
            if (it){
                capturedImageAdapter.removeLastItem()
            }
        })

    }


    private fun initCapturedImages() {
        capturedImageAdapter = CapturedImageAdapter(
            requireContext(),
            capturedImageList
        )

        binding.rvCapturedImages.apply {
            this?.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this?.adapter = capturedImageAdapter
        }


    }

    private fun initSkuDialog() {
        CreateSkuEcomDialog().show(requireFragmentManager(), "CreateSkuEcomDialog")
    }

    private fun initProjectDialog() {
        CreateProjectEcomDialog().show(requireFragmentManager(), "CreateProjectEcomDialog")
    }

    private fun showImageConfirmDialog(shootData: ShootData) {
        viewModel.shootData.value = shootData
        ConfirmReshootEcomDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOverlaysEcomBinding.inflate(inflater, container, false)

}