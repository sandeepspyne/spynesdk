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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.adapter.CompletedProjectAdapter
import com.spyneai.adapter.OngoingProjectAdapter
import com.spyneai.dashboard.adapters.OngoingDashboardAdapter
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.DashboardApi
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.ui.base.BaseFragment
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
    lateinit var categoriesResponseList: ArrayList<Data>
    lateinit var categoriesAdapter: CategoriesDashboardAdapter
    lateinit var rvDashboardCategories: RecyclerView
    lateinit var PACKAGE_NAME: String
    lateinit var btnlistener: CategoriesDashboardAdapter.BtnClickListener

    lateinit var ongoingDashboardAdapter : OngoingDashboardAdapter
    lateinit var rvOngoingShoots: RecyclerView
    lateinit var ongoingProjectList : ArrayList<com.spyneai.model.processImageService.Task>

    lateinit var completedProjectList : ArrayList<CompletedProjectResponse>
    lateinit var completedProjectAdapter : CompletedProjectAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Utilities.showProgressDialog(requireContext())

        showOngoingProjects()

        categoriesResponseList = ArrayList<Data>()
        rvDashboardCategories = RecyclerView(requireContext())

        viewModel.getCategories("utq10CZvW")

        val userId = RequestBody.create(
            MultipartBody.FORM,
            "utq10CZvW")

        viewModel.getCompletedProjects(userId)

        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            Utilities.hideProgressDialog()
            when(it){
                is Resource.Sucess -> {
                    Utilities.hideProgressDialog()
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

        viewModel.completedProjectResponse.observe(viewLifecycleOwner, Observer {
            Utilities.hideProgressDialog()
            when(it){
                is Resource.Sucess -> {
                    Utilities.hideProgressDialog()
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

    }

    private fun setCategoriesRecycler(){
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

    private fun showOngoingProjects(){
        rvOngoingShoots = RecyclerView(requireContext())
        ongoingProjectList = ProcessImagesService.tasksInProgress
        ongoingDashboardAdapter = OngoingDashboardAdapter(requireContext(),
            ongoingProjectList)

        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
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

    override fun getFragmentRepository() =
        DashboardRepository(remoteDataSource.buildApi(DashboardApi::class.java))


}