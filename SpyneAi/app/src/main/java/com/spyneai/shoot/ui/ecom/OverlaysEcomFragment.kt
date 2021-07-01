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
import java.util.ArrayList

class OverlaysEcomFragment : BaseFragment<ShootViewModel, FragmentOverlaysEcomBinding>() {

    private var showDialog = true
    lateinit var capturedImageAdapter: CapturedImageAdapter
    lateinit var capturedImageList: ArrayList<String>
    var position = 1


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initProjectDialog()


        binding.ivEndProject.setOnClickListener {
            viewModel.endShoot.value = true
        }


        //observe new image clicked
        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                if (showDialog && !it.isNullOrEmpty()) {
                    capturedImageList = ArrayList<String>()
                    position = it.size-1
                    capturedImageList.clear()
                    for (i in 0..(it.size - 1))
                        (capturedImageList as ArrayList).add(it[i].capturedImage)
                    initCapturedImages()
                    showImageConfirmDialog(it.get(it.size - 1))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

//
//        private fun showImageConfirmDialog(shootData: ShootData) {
//            viewModel.shootData.value = shootData
//            ConfirmReshootDialog().show(requireFragmentManager(), "ConfirmReshootDialog")
//        }


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

    private fun initProjectDialog() {
        CreateProjectEcomDialog().show(requireFragmentManager(), "CreateProjectAndSkuDialog")

        viewModel.isSkuCreated.observe(viewLifecycleOwner, {
            if (it) {
                Utilities.hideProgressDialog()
                binding.tvSkuName?.text = viewModel.sku.value?.skuName
                binding.tvSkuName.visibility = View.VISIBLE
            }
        })

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