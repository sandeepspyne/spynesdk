package com.spyneai.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.spyneai.dashboard.ui.dashboard.DashboardViewModel

class ViewModelFactory() : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel() as T
            else -> throw IllegalArgumentException("ViewModelClass not found")
        }
    }

}