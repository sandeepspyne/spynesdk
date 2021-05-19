package com.spyneai.dashboard.ui.dashboard

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R
import com.spyneai.dashboard.data.repository.DashboardRepository
import com.spyneai.dashboard.ui.base.BaseFragment
import com.spyneai.databinding.HomeDashboardFragmentBinding
import com.spyneai.interfaces.APiService

class HomeDashboardFragment : BaseFragment<DashboardViewModel, HomeDashboardFragmentBinding, DashboardRepository>() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun getViewModel() = DashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = HomeDashboardFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        DashboardRepository(remoteDataSource.buildApi(APiService::class.java))


}