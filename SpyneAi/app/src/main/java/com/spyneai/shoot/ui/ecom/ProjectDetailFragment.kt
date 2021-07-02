package com.spyneai.shoot.ui.ecom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentProjectDetailBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.adapters.ProjectDetailAdapter
import com.spyneai.shoot.data.ShootViewModel
import com.spyneai.shoot.data.model.ProjectDetailResponse

class ProjectDetailFragment : BaseFragment<ShootViewModel, FragmentProjectDetailBinding>() {

    lateinit var skuList : ArrayList<ProjectDetailResponse.Sku>
    lateinit var projectDetailAdapter: ProjectDetailAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skuList = ArrayList<ProjectDetailResponse.Sku>()

        viewModel.getProjectDetail(
            "13f2c605-eda4-4f2d-a512-ef781f5530bf",
//            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(),
//            viewModel.sku.value?.projectId.toString()
        "50a70e120b6e20f49890bf48a64ac948"
        )

        viewModel.projectDetailResponse.observe(viewLifecycleOwner, {
            when (it) {
                is Resource.Sucess -> {
                    Utilities.hideProgressDialog()
                    for (i in 0..(it.value.data.sku.size - 1))
                        (skuList as java.util.ArrayList).addAll(it.value.data.sku)

                    projectDetailAdapter = ProjectDetailAdapter(
                        requireContext(),
                        skuList
                    )

                    binding.rvParentProjects.apply {
                        this?.layoutManager =
                            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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