package com.spyneai.orders.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.BaseFragment
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.MyCompletedOrdersFragmentBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.CompletedSKUsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.MyCompletedOrdersAdapter
import com.spyneai.shoot.utils.log

class MyCompletedOrdersFragment :
    BaseFragment<MyOrdersViewModel, MyCompletedOrdersFragmentBinding>() {

    lateinit var myCompletedOrdersAdapter: MyCompletedOrdersAdapter
    lateinit var completedSkuList: ArrayList<CompletedSKUsResponse.Data>

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


        binding.shimmerCompletedSKU.startShimmer()

        completedSkuList = ArrayList<CompletedSKUsResponse.Data>()

        viewModel.getCompletedSKUs(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
        log("Completed SKUs(auth key): "+AppConstants.AUTH_KEY)
        viewModel.completedSKUsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is com.spyneai.base.network.Resource.Sucess -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE
                        binding.rvMyCompletedOrders.visibility = View.VISIBLE
                        if (it.value.data != null){
                            completedSkuList.clear()
                            completedSkuList.addAll(it.value.data)
                            myCompletedOrdersAdapter = MyCompletedOrdersAdapter(requireContext(),
                                completedSkuList)

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            binding.rvMyCompletedOrders.setLayoutManager(layoutManager)
                            binding.rvMyCompletedOrders.setAdapter(myCompletedOrdersAdapter)
                            myCompletedOrdersAdapter =
                                MyCompletedOrdersAdapter(
                                    requireContext(),
                                    completedSkuList
                                )
                        }
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
    override fun getViewModel() = MyOrdersViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = MyCompletedOrdersFragmentBinding.inflate(inflater, container, false)
}