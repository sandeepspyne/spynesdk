package com.spyneai.orders.ui.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.posthog.android.Properties
import com.spyneai.R
import com.spyneai.base.BaseFragment
import com.spyneai.base.network.Resource
import com.spyneai.captureFailureEvent
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.FragmentOngoingOrdersBinding
import com.spyneai.databinding.FragmentOngoingProjectsBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.MyCompletedProjectsAdapter
import com.spyneai.orders.ui.adapter.MyOngoingProjectAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log

class OngoingProjectsFragment : BaseFragment<MyOrdersViewModel, FragmentOngoingProjectsBinding>() {

    lateinit var myOngoingProjectAdapter: MyOngoingProjectAdapter
    val status = "ongoing"
    var refreshData = true
    lateinit var handler: Handler
    private var runnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        binding!!.rvMyOngoingProjects.apply {
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

        repeatRefreshData()

        log("Completed SKUs(auth key): "+ Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY))
        viewModel.getProjectsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE
                        binding.rvMyOngoingProjects.visibility = View.VISIBLE

                        if (it.value.data.project_data.isNullOrEmpty())
                            refreshData = false

                        if (it.value.data != null){

                            myOngoingProjectAdapter = MyOngoingProjectAdapter(requireContext(),
                                it.value.data.project_data, viewModel
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            binding.rvMyOngoingProjects.setLayoutManager(layoutManager)
                            binding.rvMyOngoingProjects.setAdapter(myOngoingProjectAdapter)
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE

                        if (it.errorCode == 404){
                            binding.rvMyOngoingProjects.visibility = View.GONE
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
       try {
           viewModel.getProjects(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(), status)
           runnable = Runnable {
               if (refreshData)
                   repeatRefreshData()
           }
           if (runnable != null)
               handler.postDelayed(runnable!!,15000)
       }catch (e : IllegalArgumentException){
           e.printStackTrace()
       }catch (e : Exception){
           e.printStackTrace()
       }
    }

    override fun onPause() {
        if (runnable != null)
            handler.removeCallbacks(runnable!!)
        super.onPause()
    }



    override fun getViewModel()= MyOrdersViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentOngoingProjectsBinding.inflate(inflater, container, false)
}