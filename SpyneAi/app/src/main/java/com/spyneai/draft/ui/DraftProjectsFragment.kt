package com.spyneai.draft.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentDraftProjectsBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftProjectsAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.posthog.Events

class DraftProjectsFragment : BaseFragment<DraftViewModel, FragmentDraftProjectsBinding>() {

    lateinit var draftProjectsAdapter: DraftProjectsAdapter
    lateinit var completedProjectList: ArrayList<GetProjectsResponse.Project_data>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        completedProjectList = ArrayList()

        draftProjectsAdapter = DraftProjectsAdapter(
            requireContext(),
            completedProjectList
        )

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvDraftProjects.setLayoutManager(layoutManager)
        binding.rvDraftProjects.setAdapter(draftProjectsAdapter)

        viewModel.getDrafts(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())

        viewModel.draftResponse.observe(
            viewLifecycleOwner,  {
                when (it) {
                    is Resource.Success -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE
                        binding.rvDraftProjects.visibility = View.VISIBLE


                        if (it.value.data != null){
                            completedProjectList.clear()
                            completedProjectList.addAll(it.value.data.project_data)
                            draftProjectsAdapter.notifyDataSetChanged()
                        }
                    }

                    is Resource.Failure -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE

                        if (it.errorCode == 404){
                            binding.rvDraftProjects.visibility = View.GONE
                        }else{
                            requireContext().captureFailureEvent(
                                Events.GET_COMPLETED_ORDERS_FAILED, Properties(),
                                it.errorMessage!!
                            )
                            handleApiError(it)
                        }
                    }
                }
            }
        )
    }

    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDraftProjectsBinding.inflate(inflater, container, false)

}