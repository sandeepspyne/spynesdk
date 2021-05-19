package com.spyneai.dashboard.ui.dashboard

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spyneai.R

class CompletedOrdersDashboardFragment : Fragment() {

    companion object {
        fun newInstance() = CompletedOrdersDashboardFragment()
    }

    private lateinit var viewModel: CompletedOrdersDashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.completed_orders_dashboard_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CompletedOrdersDashboardViewModel::class.java)
        // TODO: Use the ViewModel
    }

}