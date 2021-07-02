package com.spyneai.shoot.ui.ecom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSkuDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.posthog.Events
import com.spyneai.shoot.adapters.CapturedImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.ShootActivity
import java.util.*


class SkuDetailFragment : BaseFragment<ShootViewModel, FragmentSkuDetailBinding>() {

    lateinit var capturedImageAdapter: CapturedImageAdapter
    lateinit var capturedImageList: ArrayList<String>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getProjectDetail(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.projectId.toString()
        )

       viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
           when (it) {
               is Resource.Sucess -> {
                   Utilities.hideProgressDialog()
                   viewModel.totalSkuCaptured.value = it.value.data.total_sku.toString()
                   viewModel.totalImageCaptured.value = it.value.data.total_images.toString()

                   binding.tvTotalSkuCaptured.text = it.value.data.total_sku.toString()
                   binding.tvTotalImageCaptured.text = it.value.data.total_images.toString()

               }
               is Resource.Loading -> {
                   Utilities.showProgressDialog(requireContext())

               }
               is Resource.Failure -> {
                   handleApiError(it)
               }
           }
       })


        viewModel.shootList.observe(viewLifecycleOwner, {
            try {
                capturedImageList = ArrayList<String>()
                capturedImageList.clear()
                for (i in 0..(it.size - 1))
                    (capturedImageList as ArrayList).add(it[i].capturedImage)
                initCapturedImages()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        binding.btNextSku.setOnClickListener {
            viewModel.shootList.value?.clear()
            val intent = Intent(activity, ShootActivity::class.java)
            intent.putExtra("project_id", viewModel.sku.value?.projectId);
            startActivity(intent)

        }

        binding.ivAddAngle.setOnClickListener {
            viewModel.addMoreAngle.value = true
        }

        binding.tvEndProject.setOnClickListener {
            EndProjectDialog().show(requireFragmentManager(), "EndProjectDialog")
        }

    }

    private fun initCapturedImages() {
        capturedImageAdapter = CapturedImageAdapter(
            requireContext(),
            capturedImageList
        )

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