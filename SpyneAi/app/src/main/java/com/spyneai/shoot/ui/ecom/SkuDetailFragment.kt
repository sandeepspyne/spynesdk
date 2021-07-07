package com.spyneai.shoot.ui.ecom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentSkuDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.adapters.SkuImageAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.ui.ShootPortraitActivity
import java.util.*


class SkuDetailFragment : BaseFragment<ShootViewModel, FragmentSkuDetailBinding>() {

    lateinit var skuImageAdapter: SkuImageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.getProjectDetail(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.projectId.toString()
        )

       viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
           when (it) {
               is Resource.Success -> {
                   Utilities.hideProgressDialog()
                   viewModel.totalSkuCaptured.value = it.value.data.total_sku.toString()
                   viewModel.totalImageCaptured.value = it.value.data.total_images.toString()

                   binding.tvTotalSkuCaptured.text = it.value.data.total_sku.toString()


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

                    binding.tvTotalImageCaptured.text = it.size.toString()


                skuImageAdapter = SkuImageAdapter(
                    requireContext(),
                    it
                )

                binding.rvSkuImages.apply {
                    this?.layoutManager =
                        GridLayoutManager(requireContext(),  3)
                    this?.adapter = skuImageAdapter
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        binding.btNextSku.setOnClickListener {
            viewModel.shootList.value?.clear()
            val intent = Intent(activity, ShootPortraitActivity::class.java)
            intent.putExtra("project_id", viewModel.sku.value?.projectId);
            startActivity(intent)

        }

        binding.ivAddAngle.setOnClickListener {
            viewModel.addMoreAngle.value = true
        }
        binding.tvAddAngle.setOnClickListener {
            viewModel.addMoreAngle.value = true
        }

        binding.tvEndProject.setOnClickListener {
            EndProjectDialog().show(requireFragmentManager(), "EndProjectDialog")
        }

    }

    private fun initCapturedImages() {



    }


    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSkuDetailBinding.inflate(inflater, container, false)

}