package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.posthog.android.Properties
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentCompletedSkusBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.SkusAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log

class CompletedSkusFragment : BaseFragment<MyOrdersViewModel, FragmentCompletedSkusBinding>() {

    lateinit var skusAdapter: SkusAdapter
    val status = "completed"
    var refreshData = true
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    lateinit var skuList: ArrayList<GetProjectsResponse.Sku>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.rvSkus.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext(), LinearLayoutManager.VERTICAL,
                    false
                )
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        skuList = ArrayList<GetProjectsResponse.Sku>()


        binding.shimmerCompletedSKU.startShimmer()
        repeatRefreshData()


        log("Completed SKUs(auth key): "+ Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY))
        viewModel.getProjectsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE
                        binding.rvSkus.visibility = View.VISIBLE

                        if (it.value.data.project_data.isNullOrEmpty())
                            refreshData = false

                        if (it.value.data != null){

                            binding.tvTotalSku.text = it.value.data.total_skus.toString()

                            skuList.clear()
                            for (i in 0..it.value.data.project_data.size){
                                if (i == viewModel.position.value){
                                    skuList.addAll(it.value.data.project_data[i].sku)
                                    binding.tvProjectName.text = it.value.data.project_data[i].project_name
                                }

                            }

                            skusAdapter = SkusAdapter(requireContext(),
                                it.value.data.project_data, viewModel, skuList
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            binding.rvSkus.setLayoutManager(layoutManager)
                            binding.rvSkus.setAdapter(skusAdapter)
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        refreshData = false
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE

                        if (it.errorCode == 404){
                            binding.rvSkus.visibility = View.GONE
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

    fun repeatRefreshData(){
        viewModel.getProjects(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(), status)
        handler = Handler()
        runnable = Runnable {
            if (refreshData)
                repeatRefreshData()  }
        handler.postDelayed(runnable,15000)
    }

    override fun onPause() {
        handler.removeCallbacks(runnable)
        super.onPause()
    }


    override fun getViewModel() = MyOrdersViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCompletedSkusBinding.inflate(inflater, container, false)


}