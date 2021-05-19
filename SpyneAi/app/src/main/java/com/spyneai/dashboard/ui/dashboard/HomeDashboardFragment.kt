package com.spyneai.dashboard.ui.dashboard

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.spyneai.R
import com.spyneai.adapter.CategoriesDashboardAdapter
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.network.DashboardApi
import com.spyneai.dashboard.network.Resource
import com.spyneai.dashboard.ui.base.BaseFragment
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.interfaces.APiService
import com.spyneai.model.categories.Data
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities

class HomeDashboardFragment :
    BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding, DashboardRepository>() {

    lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE: Int = 1
    lateinit var categoriesResponseList: ArrayList<Data>
    lateinit var categoriesAdapter: CategoriesDashboardAdapter
    lateinit var rvDashboardCategories: RecyclerView
    lateinit var PACKAGE_NAME: String

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.categoriesResponse.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Sucess -> {
                    Toast.makeText(requireContext(), it.value.payload.data.size.toString(), Toast.LENGTH_SHORT).show()
                    categoriesResponseList.addAll(it.value.payload.data)
                }
                is Resource.Failure -> {
                    Toast.makeText(requireContext(), "Api Failure", Toast.LENGTH_SHORT).show()
                }
            }
        })


    }


    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        DashboardRepository(remoteDataSource.buildApi(DashboardApi::class.java))


}