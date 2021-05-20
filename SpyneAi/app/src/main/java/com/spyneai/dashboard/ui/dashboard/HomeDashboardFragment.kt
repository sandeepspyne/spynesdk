package com.spyneai.dashboard.ui.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.adapter.CompletedProjectAdapter
import com.spyneai.adapter.OngoingProjectAdapter
import com.spyneai.dashboard.adapters.CompletedDashboardAdapter
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.DashboardApi
import com.spyneai.dashboard.network.RemoteDataSourceSpyneAi
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.ui.base.BaseFragment
import com.spyneai.dashboard.ui.base.ViewModelFactory
import com.spyneai.dashboard.ui.handleApiError
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.interfaces.APiService
import com.spyneai.model.categories.Data
import com.spyneai.model.projects.CompletedProjectResponse
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.service.ProcessImagesService
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_ongoing_orders.*
import kotlinx.android.synthetic.main.home_dashboard_fragment.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding, DashboardRepository>() {

    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1
    lateinit var PACKAGE_NAME: String
    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    lateinit var categoriesResponseList: ArrayList<Data>
    lateinit var categoriesAdapter: CategoriesDashboardAdapter

    lateinit var ongoingDashboardAdapter : OngoingDashboardAdapter
    lateinit var ongoingProjectList : ArrayList<com.spyneai.model.processImageService.Task>

    lateinit var completedProjectList : ArrayList<CompletedProjectResponse>
    lateinit var completedDashboardAdapter : CompletedDashboardAdapter

    var handelBaseUrl = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Utilities.showProgressDialog(requireContext())

        setOngoingProjectRecycler()

        categoriesResponseList = ArrayList<Data>()

        viewModel.getCategories("utq10CZvW")

        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    Toast.makeText(requireContext(), it.value.payload.data.size.toString(), Toast.LENGTH_SHORT).show()
                    categoriesResponseList.addAll(it.value.payload.data)
//                    categoriesAdapter.notifyDataSetChanged()
                    setCategoriesRecycler()
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })

        val userId = RequestBody.create(
            MultipartBody.FORM,
            "utq10CZvW")

        handelBaseUrl = 1
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory).get(getViewModel())

        viewModel.getCompletedProjects(userId)
        completedProjectList = ArrayList<CompletedProjectResponse>()

        viewModel.completedProjectResponse.observe(viewLifecycleOwner, Observer {
            Utilities.hideProgressDialog()
            when(it){
                is Resource.Sucess -> {
                    Toast.makeText(requireContext(), it.value.toString(), Toast.LENGTH_SHORT).show()
                    completedProjectList.addAll(it.value)
                    completedProjectList.reverse()
//                    categoriesAdapter.notifyDataSetChanged()
                    setCompletedProjectRecycler()
                }
//                is Resource.Loading -> {
//                    Utilities.showProgressDialog(requireContext())
//                }
                is Resource.Failure -> {
                    handleApiError(it)
                }
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun setCompletedProjectRecycler(){
        Utilities.hideProgressDialog()
        completedDashboardAdapter = CompletedDashboardAdapter(requireContext(),
            completedProjectList)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCompletedShoots.setLayoutManager(layoutManager)
        rvCompletedShoots.setAdapter(completedDashboardAdapter)

    }

    private fun setCategoriesRecycler(){
        Utilities.hideProgressDialog()
        categoriesAdapter = CategoriesDashboardAdapter(requireContext(), categoriesResponseList,
            object : CategoriesDashboardAdapter.BtnClickListener {
                override fun onBtnClick(position: Int) {
                    if (position < 3) {
                        Utilities.savePrefrence(
                            requireContext(),
                            AppConstants.CATEGORY_NAME,
                            categoriesResponseList[position].displayName
                        )
//                        setShoot(categoriesResponseList, position)
                    } else
                        Toast.makeText(
                            requireContext(),
                            "Coming Soon !",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            })
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rvDashboardCategories.setLayoutManager(layoutManager)
        rvDashboardCategories.setAdapter(categoriesAdapter)
    }

    private fun setOngoingProjectRecycler(){
        ongoingProjectList = ProcessImagesService.tasksInProgress
        ongoingDashboardAdapter = OngoingDashboardAdapter(requireContext(),
            ongoingProjectList)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvOngoingShoots.setLayoutManager(layoutManager)
        rvOngoingShoots.setAdapter(ongoingDashboardAdapter)
        refreshList()
        showHideRecyclerView()
    }

    private fun refreshList(){
        Handler(Looper.getMainLooper()).postDelayed({
            ongoingDashboardAdapter.notifyDataSetChanged()
            refreshList()
        }, 3000)

    }

    private fun showHideRecyclerView(){
        if (ongoingProjectList.size == 0)
            groupOngoingProjects.visibility = View.GONE
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() : DashboardRepository{
        return when(handelBaseUrl){
            0 -> DashboardRepository(RemoteDataSourceSpyneAi("https://api.spyne.ai/").buildApi(DashboardApi::class.java))
            1 -> DashboardRepository(RemoteDataSourceSpyneAi("https://www.clippr.ai/api/").buildApi(DashboardApi::class.java))

            else -> DashboardRepository(RemoteDataSourceSpyneAi("https://api.spyne.ai/").buildApi(DashboardApi::class.java))
        }
    }



}