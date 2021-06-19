package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.MyOngoingOrdersFragmentBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetOngoingSkusResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.MyCompletedOrdersAdapter
import com.spyneai.orders.ui.adapter.MyOngoingOrdersAdapter

class MyOngoingOrdersFragment : BaseFragment<MyOrdersViewModel, MyOngoingOrdersFragmentBinding>() {

    lateinit var myOngoingOrdersAdapter: MyOngoingOrdersAdapter
    lateinit var ongoingSkuList: ArrayList<GetOngoingSkusResponse.Data>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rvMyOngoingOrders.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL, false
            )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.shimmerOngoingSKU.startShimmer()

        ongoingSkuList = ArrayList<GetOngoingSkusResponse.Data>()
        setOngoingSkuRecycler()

        viewModel.getOngoingSKUs(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString())
        viewModel.getOngoingSkusResponse.observe(
            viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it) {
                    is Resource.Sucess -> {

                        binding.shimmerOngoingSKU.stopShimmer()
                        binding.shimmerOngoingSKU.visibility = View.GONE
                        binding.rvMyOngoingOrders.visibility = View.VISIBLE

                        ongoingSkuList.addAll(it.value.data)
                        myOngoingOrdersAdapter = MyOngoingOrdersAdapter(requireContext(),
                            ongoingSkuList)

                        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        binding.rvMyOngoingOrders.setLayoutManager(layoutManager)
                        binding.rvMyOngoingOrders.setAdapter(myOngoingOrdersAdapter)
                        myOngoingOrdersAdapter =
                            MyOngoingOrdersAdapter(
                                requireContext(),
                                ongoingSkuList
                            )
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        handleApiError(it)
                    }

                }
            }
        )
    }

    private fun setOngoingSkuRecycler(){

    }

    override fun getViewModel() = MyOrdersViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = MyOngoingOrdersFragmentBinding.inflate(inflater, container, false)
}