package com.spyneai.draft.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentDraftProjectsBinding
import com.spyneai.draft.data.DraftViewModel
import com.spyneai.draft.ui.adapter.DraftProjectsAdapter
import com.spyneai.draft.ui.adapter.LocalDraftProjectsAdapter
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse

import kotlinx.coroutines.*


class DraftProjectsFragment : BaseFragment<DraftViewModel, FragmentDraftProjectsBinding>() {

    lateinit var draftProjectsAdapter: DraftProjectsAdapter
    lateinit var localDraftProjectsAdapter: LocalDraftProjectsAdapter
    lateinit var completedProjectList: ArrayList<GetProjectsResponse.Project_data>
    lateinit var draftProjectList: ArrayList<com.spyneai.shoot.repository.model.project.Project>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        completedProjectList = ArrayList()
        draftProjectList = ArrayList()

        draftProjectsAdapter = DraftProjectsAdapter(
            requireContext(),
            completedProjectList
        )

        localDraftProjectsAdapter = LocalDraftProjectsAdapter(
            requireContext(),
            draftProjectList
        )

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvDraftProjects.layoutManager = layoutManager

        binding.shimmerCompletedSKU.stopShimmer()
        binding.shimmerCompletedSKU.visibility = View.GONE
        binding.rvDraftProjects.visibility = View.VISIBLE
        showLocalDraftProjects()

//        viewModel.getDrafts(
//            Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString()
//        )
//
//        viewModel.draftResponse.observe(
//            viewLifecycleOwner, {
//                when (it) {
//                    is Resource.Success -> {
//                        binding.shimmerCompletedSKU.stopShimmer()
//                        binding.shimmerCompletedSKU.visibility = View.GONE
//                        binding.rvDraftProjects.visibility = View.VISIBLE
//
//                        if (it.value.data != null) {
//                            val localDraftList = viewModel.getDraftsFromLocal()
//
//                            if (it.value.data.project_data.size == localDraftList.size) {
//                                draftProjectList.clear()
//                                draftProjectList.addAll(viewModel.getDraftsFromLocal())
//                                binding.rvDraftProjects.adapter = localDraftProjectsAdapter
//                                localDraftProjectsAdapter.notifyDataSetChanged()
//                            } else {
//                                completedProjectList.clear()
//                                completedProjectList.addAll(it.value.data.project_data)
//                                binding.rvDraftProjects.adapter = draftProjectsAdapter
//                                draftProjectsAdapter.notifyDataSetChanged()
//                            }
//                        }
//                    }
//
//                    is Resource.Failure -> {
//                        binding.shimmerCompletedSKU.stopShimmer()
//                        binding.shimmerCompletedSKU.visibility = View.GONE
//                        binding.rvDraftProjects.visibility = View.VISIBLE
//
//                        if (it.errorCode == 404) {
//                            binding.rvDraftProjects.visibility = View.GONE
//                        } else {
//                            requireContext().captureFailureEvent(
//                                Events.GET_COMPLETED_ORDERS_FAILED, HashMap<String, Any?>(),
//                                it.errorMessage!!
//                            )
//                            handleApiError(it)
//                        }
//                    }
//                }
//            }
//        )
    }

    private fun showLocalDraftProjects() {
        GlobalScope.launch(Dispatchers.IO) {
            val localDraftList = viewModel.getDraftsFromLocal()

            GlobalScope.launch(Dispatchers.Main){
                draftProjectList.clear()
                draftProjectList.addAll(localDraftList)
                binding.rvDraftProjects.adapter = localDraftProjectsAdapter
                localDraftProjectsAdapter.notifyDataSetChanged()
            }

        }


    }

    override fun getViewModel() = DraftViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDraftProjectsBinding.inflate(inflater, container, false)

}