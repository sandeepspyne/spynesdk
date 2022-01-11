package com.spyneai.orders.data.paging

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.spyneai.base.BaseFragment
import com.spyneai.databinding.FragmentOngoingProjectsBinding
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class PagedFragment : BaseFragment<MyOrdersViewModel, FragmentOngoingProjectsBinding>() {

    lateinit var adapter: ProjectPagedAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProjectPagedAdapter()

        binding.rvMyOngoingProjects.layoutManager  =
            LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL,
                false
            )

        val loaderStateAdapter = LoaderStateAdapter { adapter.retry() }
        binding.rvMyOngoingProjects.adapter = adapter.withLoadStateFooter(loaderStateAdapter)

        //binding.rvMyOngoingProjects.adapter = adapter

        fetchProjects()
    }

    private fun fetchProjects() {
        lifecycleScope.launch {
            viewModel.getAllProjects().distinctUntilChanged().collectLatest {
                if (!binding.rvMyOngoingProjects.isVisible){
                    binding.shimmerCompletedSKU.stopShimmer()
                    binding.shimmerCompletedSKU.visibility = View.GONE
                    binding.rvMyOngoingProjects.visibility = View.VISIBLE
                }
                adapter.submitData(it)
            }
        }
    }

    override fun getViewModel()= MyOrdersViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOngoingProjectsBinding.inflate(inflater, container, false)
}