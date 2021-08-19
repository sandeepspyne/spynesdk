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
import com.spyneai.databinding.FragmentCompletedProjectsBinding
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.orders.ui.adapter.MyCompletedProjectsAdapter
import com.spyneai.posthog.Events
import com.spyneai.shoot.utils.log

class CompletedProjectsFragment : BaseFragment<MyOrdersViewModel, FragmentCompletedProjectsBinding>() {

    lateinit var myCompletedProjectsAdapter: MyCompletedProjectsAdapter
    val status = "completed"
    var refreshData = true
    lateinit var handler: Handler
    private var runnable: Runnable? = null
    lateinit var completedProjectList: ArrayList<GetProjectsResponse.Project_data>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        binding!!.rvMyCompletedProjects.apply {
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

        completedProjectList = ArrayList()


        repeatRefreshData()
        log("Completed SKUs(auth key): "+ Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY))
        viewModel.getCompletedProjectsResponse.observe(
            viewLifecycleOwner, Observer {
                when (it) {
                    is Resource.Success -> {
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE
                        binding.rvMyCompletedProjects.visibility = View.VISIBLE

                        if (it.value.data.project_data.isNullOrEmpty())
                            refreshData = false

                        if (it.value.data != null){
                            completedProjectList.clear()
                            completedProjectList.addAll(it.value.data.project_data)
                            myCompletedProjectsAdapter = MyCompletedProjectsAdapter(requireContext(),
                                completedProjectList, viewModel
                            )

                            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            binding.rvMyCompletedProjects.setLayoutManager(layoutManager)
                            binding.rvMyCompletedProjects.setAdapter(myCompletedProjectsAdapter)
                        }
                    }
                    is Resource.Loading -> {

                    }
                    is Resource.Failure -> {
                        refreshData = false
                        binding.shimmerCompletedSKU.stopShimmer()
                        binding.shimmerCompletedSKU.visibility = View.GONE

                        if (it.errorCode == 404){
                            binding.rvMyCompletedProjects.visibility = View.GONE
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
            viewModel.getCompletedProjects(Utilities.getPreference(requireContext(), AppConstants.AUTH_KEY).toString(), status)
            runnable = Runnable {
                if (refreshData)
                    repeatRefreshData()  }
            if (runnable != null)
                handler.postDelayed(runnable!!,10000)
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

    override fun getViewModel() = MyOrdersViewModel::class.java
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCompletedProjectsBinding.inflate(inflater, container, false)

}