package com.spyneai.shoot.ui.ecomwithgrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentGridEcomBinding
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ShootData
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.ConfirmReshootEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateProjectEcomDialog
import com.spyneai.shoot.ui.ecomwithgrid.dialogs.CreateSkuEcomDialog
import com.spyneai.shoot.utils.log
import java.util.*

class GridEcomFragment : BaseFragment<ShootViewModel, FragmentGridEcomBinding>() {


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
            if (viewModel.fromDrafts){
                if (viewModel.isSkuCreated.value == null
                    && viewModel.isSubCategoryConfirmed.value == null)
                    initSkuDialog()
            }else {
                if (viewModel.isSkuCreated.value == null)
                    initSkuDialog()

            }
        }

        binding.ivEndProject.setOnClickListener {
            if (viewModel.fromDrafts){
                viewModel.stopShoot.value = true
            }else {
                if (viewModel.isStopCaptureClickable)
                    viewModel.stopShoot.value = true
            }
        }


        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (!it.isNullOrEmpty()) {
                    capturedImageList = ArrayList<String>()
                    position = it.size - 1
                    capturedImageList.clear()
                    for (i in 0..(it.size - 1))
                        (capturedImageList as ArrayList).add(it[i].capturedImage)
                    initCapturedImages()

                    if (viewModel.showDialog)
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

                if (!viewModel.fromDrafts)
                    viewModel.shootNumber.value = 0
            }
        })

        viewModel.reshootCapturedImage.observe(viewLifecycleOwner,{
            if (it){
                capturedImageAdapter.removeLastItem()
            }
        })

        viewModel.confirmCapturedImage.observe(viewLifecycleOwner,{
            if (it){
                binding.tvImageCount.text = viewModel.shootList.value!!.size.toString()
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

    override fun onResume() {
        super.onResume()

        if (viewModel.fromDrafts){
            if (viewModel.showLeveler.value == null || viewModel.showLeveler.value == false){
                viewModel.showLeveler.value = true
                viewModel.showDialog = true
            }

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
    ) = FragmentGridEcomBinding.inflate(inflater, container, false)

}