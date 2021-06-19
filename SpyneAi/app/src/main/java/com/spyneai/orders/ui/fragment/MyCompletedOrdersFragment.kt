package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.MyCompletedOrdersFragmentBinding
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetCompletedSKUsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.MyCompletedOrdersAdapter
import com.spyneai.service.ProcessImagesService
import kotlinx.android.synthetic.main.home_dashboard_fragment.*

class MyCompletedOrdersFragment :
    BaseFragment<MyOrdersViewModel, MyCompletedOrdersFragmentBinding>() {

    lateinit var tokenId: String
    lateinit var myCompletedOrdersAdapter: MyCompletedOrdersAdapter
    lateinit var completedSkuList: ArrayList<GetCompletedSKUsResponse>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rvMyCompletedOrders.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext(), LinearLayoutManager.VERTICAL,
                    false
                )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tokenId = Utilities.getPreference(requireContext(), AppConstants.tokenId).toString()
        completedSkuList = ArrayList<GetCompletedSKUsResponse>()
        setCompletedSkuRecycler()

        viewModel.getCompletedSKUs(tokenId)
        viewModel.getCompletedSKUsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is com.spyneai.base.network.Resource.Sucess -> {
                        completedSkuList.add(it.value)
                        myCompletedOrdersAdapter =
                            MyCompletedOrdersAdapter(
                                requireContext(),
                                completedSkuList
                            )
                    }
                    is com.spyneai.base.network.Resource.Loading -> {

                    }
                    is com.spyneai.base.network.Resource.Failure -> {
                        handleApiError(it)
                    }

                }
            }
        )
    }

    private fun setCompletedSkuRecycler(){
        myCompletedOrdersAdapter = MyCompletedOrdersAdapter(requireContext(),
            completedSkuList)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvMyCompletedOrders.setLayoutManager(layoutManager)
        binding.rvMyCompletedOrders.setAdapter(myCompletedOrdersAdapter)
    }

    override fun getViewModel() = MyOrdersViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = MyCompletedOrdersFragmentBinding.inflate(inflater, container, false)
}