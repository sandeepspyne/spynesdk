package com.spyneai.dashboard.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spyneai.dashboard.data.DashboardViewModel
import com.spyneai.orders.data.viewmodel.MyOrdersViewModel
import com.spyneai.processedimages.ui.data.ProcessedViewModel
import com.spyneai.shoot.data.ProcessViewModel
import com.spyneai.shoot.data.ShootViewModel

class ViewModelFactory() : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel() as T
            modelClass.isAssignableFrom(ShootViewModel::class.java) -> ShootViewModel() as T
            modelClass.isAssignableFrom(ProcessViewModel::class.java) -> ProcessViewModel() as T
            modelClass.isAssignableFrom(MyOrdersViewModel::class.java) -> MyOrdersViewModel() as T
            modelClass.isAssignableFrom(ProcessedViewModel::class.java) -> ProcessedViewModel() as T
            else -> throw IllegalArgumentException("ViewModelClass not found")
        }
    }

}