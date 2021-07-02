package com.spyneai.shoot.ui.ecom

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.HomeDashboardFragment
import com.spyneai.dashboard.ui.MainDashboardActivity
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.adapters.ProjectDetailAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ProjectDetailResponse
import com.spyneai.shoot.ui.ShootActivity

class ProjectDetailFragment : BaseFragment<ShootViewModel, FragmentProjectDetailBinding>() {

    lateinit var skuList : ArrayList<ProjectDetailResponse.Sku>
    lateinit var imageList : ArrayList<ProjectDetailResponse.Images>
    lateinit var projectDetailAdapter: ProjectDetailAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skuList = ArrayList<ProjectDetailResponse.Sku>()
        imageList = ArrayList<ProjectDetailResponse.Images>()

        viewModel.getProjectDetail(
            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
            viewModel.sku.value?.projectId.toString()
        )

        binding.btHome.setOnClickListener {
            val intent = Intent(activity, MainDashboardActivity::class.java)
            startActivity(intent)
        }

        viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Sucess -> {
                    Utilities.hideProgressDialog()
                    for (i in 0..(it.value.data.sku.size - 1))
                        (skuList as java.util.ArrayList).addAll(it.value.data.sku)

                    for (i in 0..(it.value.data.sku.size - 1))
                        (imageList as java.util.ArrayList).addAll(it.value.data.sku[i].images)

                    binding.tvTotalSkuCaptured.text = it.value.data.total_sku.toString()
                    binding.tvTotalImageCaptured.text = it.value.data.total_images.toString()

                    projectDetailAdapter = ProjectDetailAdapter(
                        requireContext(),
                        skuList, imageList
                    )

                    binding.rvParentProjects.apply {
                        this?.layoutManager =
                            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        this?.adapter = projectDetailAdapter
                    }

                }
                is Resource.Loading -> {
                    Utilities.showProgressDialog(requireContext())

                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })

    }

    override fun getViewModel() = ShootViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProjectDetailBinding.inflate(inflater, container, false)

}